

package keeno.usap.analysis.pta.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.MutableInt;
import keeno.usap.util.collection.Maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Profiler to help identify analysis hot spots in the analyzed program
 * and assist performance tuning for pointer analysis.
 */
public class Profiler implements Plugin {

    private static final Logger logger = LogManager.getLogger(Profiler.class);

    private static final String PROFILE_FILE = "pta-profile.txt";

    /**
     * Reports the results for top N elements.
     */
    private static final int TOP_N = 100;

    private Solver solver;

    private CSManager csManager;

    private final Map<CSVar, MutableInt> csVarVisited = Maps.newMap();

    private final Map<Var, MutableInt> varVisited = Maps.newMap();

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        this.csManager = solver.getCSManager();
    }

    @Override
    public void onNewPointsToSet(CSVar csVar, PointsToSet pts) {
        csVarVisited.computeIfAbsent(csVar, __ -> new MutableInt(0)).add(1);
        varVisited.computeIfAbsent(csVar.getVar(), __ -> new MutableInt(0)).add(1);
    }

    @Override
    public void onFinish() {
        File outFile = new File(World.get().getOptions().getOutputDir(), PROFILE_FILE);
        try (PrintStream out = new PrintStream(new FileOutputStream(outFile))) {
            logger.info("Dumping pointer analysis profile to {}",
                    outFile.getAbsolutePath());
            // report variables
            reportTop(out, "frequently-visited variables",
                    varVisited, v -> v.getMethod() + "/" + v.getName());
            reportTop(out, "frequently-visited CS variables",
                    csVarVisited, CSVar::toString);
            // count and report methods
            Map<JMethod, MutableInt> methodVarVisited = Maps.newMap();
            varVisited.forEach((v, times) ->
                    methodVarVisited.computeIfAbsent(v.getMethod(),
                                    __ -> new MutableInt(0))
                            .add(times.intValue()));
            reportTop(out, "method containers (of frequently-visited variables)",
                    methodVarVisited, JMethod::toString);
            Map<CSMethod, MutableInt> csMethodVarVisited = Maps.newMap();
            csVarVisited.forEach((v, times) -> {
                CSMethod method = csManager.getCSMethod(
                        v.getContext(), v.getVar().getMethod());
                csMethodVarVisited.computeIfAbsent(method,
                                __ -> new MutableInt(0))
                        .add(times.intValue());
            });
            reportTop(out, "CS method containers (of frequently-visited CS variables)",
                    csMethodVarVisited, CSMethod::toString);
            // count and report classes
            Map<JClass, MutableInt> classVarVisited = Maps.newMap();
            methodVarVisited.forEach((m, times) ->
                    classVarVisited.computeIfAbsent(m.getDeclaringClass(),
                                    __ -> new MutableInt(0))
                            .add(times.intValue()));
            reportTop(out, "class containers (of frequently-visited variables)",
                    classVarVisited, JClass::toString);
            // count and report points-to sets counter
            PointerAnalysisResult ptaResult = solver.getResult();
            reportPtsTop(out, "points-to set of variables", ptaResult.getCSVars());
            reportPtsTop(out, "points-to set of static fields", ptaResult.getStaticFields());
            reportPtsTop(out, "points-to set of instance fields", ptaResult.getInstanceFields());
            reportPtsTop(out, "points-to set of array indexes", ptaResult.getArrayIndexes());
        } catch (FileNotFoundException e) {
            logger.warn("Failed to write pointer analysis profile to {}, caused by {}",
                    outFile.getAbsolutePath(), e);
        }
    }

    private static void reportPtsTop(
            PrintStream out, String desc, Collection<? extends Pointer> pointers) {
        Map<Pointer, Integer> map = pointers.stream()
                .collect(Collectors.toMap(Function.identity(),
                        o -> o.getObjects().size()));
        reportTop(out, desc, map, Object::toString);
    }

    private static <E> void reportTop(
            PrintStream out, String desc,
            Map<E, ? extends Number> visited, Function<E, String> toString) {
        out.printf("Top %d %s:%n", TOP_N, desc);
        // obtain top N elements
        PriorityQueue<E> topQueue = new PriorityQueue<>(TOP_N,
                Comparator.comparingInt(e -> visited.get(e).intValue()));
        visited.keySet().forEach(e -> {
            topQueue.add(e);
            if (topQueue.size() > TOP_N) {
                topQueue.poll();
            }
        });
        // the topQueue is a minimum heap, thus, to report elements
        // from larger to small, we need to reverse the stream
        topQueue.stream()
                .sorted(topQueue.comparator().reversed())
                .forEach(e -> out.printf("%s\t%s%n",
                        visited.get(e).intValue(), toString.apply(e)));
        out.println();
    }
}
