

package keeno.usap.analysis.pta.toolkit.zipper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.graph.flowgraph.InstanceNode;
import keeno.usap.analysis.graph.flowgraph.Node;
import keeno.usap.analysis.graph.flowgraph.ObjectFlowGraph;
import keeno.usap.analysis.graph.flowgraph.VarNode;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.toolkit.PointerAnalysisResultEx;
import keeno.usap.analysis.pta.toolkit.PointerAnalysisResultExImpl;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.New;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.MutableInt;
import keeno.usap.util.Timer;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Sets;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Zipper {

    private static final Logger logger = LogManager.getLogger(Zipper.class);

    private static final float DEFAULT_PV = 0.05f;

    private final PointerAnalysisResultEx pta;

    private final boolean isExpress;

    /**
     * Percentage value, i.e., the threshold for Zipper-e.
     */
    private final float pv;

    private final ObjectAllocationGraph oag;

    private final PotentialContextElement pce;

    private final ObjectFlowGraph ofg;

    private AtomicInteger totalPFGNodes;

    private AtomicInteger totalPFGEdges;

    private Map<Type, Collection<JMethod>> pcmMap;

    private int pcmThreshold;

    private Map<JMethod, MutableInt> methodPts;

    /**
     * Parses Zipper argument and runs Zipper.
     */
    public static Set<JMethod> run(PointerAnalysisResult pta, String arg) {
        boolean isExpress;
        float pv;
        if (arg.equals("zipper")) {
            isExpress = false;
            pv = 1;
        } else if (arg.equals("zipper-e")) {
            isExpress = true;
            pv = DEFAULT_PV;
        } else if (arg.startsWith("zipper-e=")) { // zipper-e=pv
            isExpress = true;
            pv = Float.parseFloat(arg.split("=")[1]);
        } else {
            throw new IllegalArgumentException("Illegal Zipper argument: " + arg);
        }
        return new Zipper(pta, isExpress, pv)
                .selectPrecisionCriticalMethods();
    }

    public Zipper(PointerAnalysisResult ptaBase, boolean isExpress, float pv) {
        this.pta = new PointerAnalysisResultExImpl(ptaBase, true);
        this.isExpress = isExpress;
        this.pv = pv;
        this.oag = Timer.runAndCount(() -> new ObjectAllocationGraph(pta),
                "Building OAG", Level.INFO);
        this.pce = Timer.runAndCount(() -> new PotentialContextElement(pta, oag),
                "Building PCE", Level.INFO);
        this.ofg = ptaBase.getObjectFlowGraph();
        logger.info("{} nodes in OFG", ofg.getNodes().size());
        logger.info("{} edges in OFG",
                ofg.getNodes().stream().mapToInt(ofg::getOutDegreeOf).sum());
    }

    /**
     * @return a set of precision-critical methods that should be analyzed
     * context-sensitively.
     */
    public Set<JMethod> selectPrecisionCriticalMethods() {
        totalPFGNodes = new AtomicInteger(0);
        totalPFGEdges = new AtomicInteger(0);
        pcmMap = Maps.newConcurrentMap(1024);

        // prepare information for Zipper-e
        if (isExpress) {
            PointerAnalysisResult pta = this.pta.getBase();
            int totalPts = 0;
            methodPts = Maps.newMap(pta.getCallGraph().getNumberOfMethods());
            for (Var var : pta.getVars()) {
                int size = pta.getPointsToSet(var).size();
                if (size > 0) {
                    totalPts += size;
                    methodPts.computeIfAbsent(var.getMethod(),
                                    __ -> new MutableInt(0))
                            .add(size);
                }
            }
            pcmThreshold = (int) (pv * totalPts);
        }

        // build and analyze precision-flow graphs
        Set<Type> types = pta.getObjectTypes();
        Timer.runAndCount(() -> types.parallelStream().forEach(this::analyze),
                "Building and analyzing PFG", Level.INFO);
        logger.info("#types: {}", types.size());
        logger.info("#avg. nodes in PFG: {}", totalPFGNodes.get() / types.size());
        logger.info("#avg. edges in PFG: {}", totalPFGEdges.get() / types.size());

        // collect all precision-critical methods
        Set<JMethod> pcms = pcmMap.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
        logger.info("#precision-critical methods: {}", pcms.size());
        return pcms;
    }

    private void analyze(Type type) {
        PrecisionFlowGraph pfg = new PFGBuilder(pta, ofg, oag, pce, type).build();
        totalPFGNodes.addAndGet(pfg.getNumberOfNodes());
        totalPFGEdges.addAndGet(pfg.getNodes()
                .stream()
                .mapToInt(pfg::getOutDegreeOf)
                .sum());
        Set<JMethod> pcms = getPrecisionCriticalMethods(pfg);
        if (!pcms.isEmpty()) {
            pcmMap.put(type, pcms);
        }
    }

    private Set<JMethod> getPrecisionCriticalMethods(PrecisionFlowGraph pfg) {
        Set<JMethod> pcms = getFlowNodes(pfg)
                .stream()
                .map(Zipper::node2Method)
                .filter(Objects::nonNull)
                .filter(pce.pceMethodsOf(pfg.getType())::contains)
                .collect(Collectors.toUnmodifiableSet());
        if (isExpress) {
            int accPts = 0;
            for (JMethod m : pcms) {
                accPts += methodPts.get(m).intValue();
            }
            if (accPts > pcmThreshold) {
                // clear precision-critical method group whose accumulative
                // points-to size exceeds the threshold
                pcms = Set.of();
            }
        }
        return pcms;
    }

    private static Set<Node> getFlowNodes(PrecisionFlowGraph pfg) {
        Set<Node> visited = Sets.newSet();
        for (VarNode outNode : pfg.getOutNodes()) {
            Deque<Node> workList = new ArrayDeque<>();
            workList.add(outNode);
            while (!workList.isEmpty()) {
                Node node = workList.poll();
                if (visited.add(node)) {
                    pfg.getPredsOf(node)
                            .stream()
                            .filter(Predicate.not(visited::contains))
                            .forEach(workList::add);
                }
            }
        }
        return visited;
    }

    /**
     * @return containing method of {@code node}.
     */
    @Nullable
    private static JMethod node2Method(Node node) {
        if (node instanceof VarNode varNode) {
            return varNode.getVar().getMethod();
        } else {
            Obj base = ((InstanceNode) node).getBase();
            if (base.getAllocation() instanceof New newStmt) {
                return newStmt.getContainer();
            }
        }
        return null;
    }
}
