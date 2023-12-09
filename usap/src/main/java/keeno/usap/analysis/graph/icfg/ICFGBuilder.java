

package keeno.usap.analysis.graph.icfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.analysis.ProgramAnalysis;
import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.graph.callgraph.CallGraphBuilder;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGBuilder;
import keeno.usap.analysis.graph.cfg.CFGDumper;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.Indexer;
import keeno.usap.util.SimpleIndexer;
import keeno.usap.util.graph.DotAttributes;
import keeno.usap.util.graph.DotDumper;

import java.io.File;

public class ICFGBuilder extends ProgramAnalysis<ICFG<JMethod, Stmt>> {

    public static final String ID = "icfg";

    private static final Logger logger = LogManager.getLogger(ICFGBuilder.class);

    private final boolean isDump;

    public ICFGBuilder(AnalysisConfig config) {
        super(config);
        isDump = getOptions().getBoolean("dump");
    }

    @Override
    public ICFG<JMethod, Stmt> analyze() {
        CallGraph<Stmt, JMethod> callGraph = World.get().getResult(CallGraphBuilder.ID);
        ICFG<JMethod, Stmt> icfg = new DefaultICFG(callGraph);
        if (isDump) {
            dumpICFG(icfg);
        }
        return icfg;
    }

    private static void dumpICFG(ICFG<JMethod, Stmt> icfg) {
        JMethod mainMethod;
        String fileName;
        if ((mainMethod = World.get().getMainMethod()) != null) {
            fileName = mainMethod.getDeclaringClass() + "-icfg.dot";
        } else {
            fileName = "icfg.dot";
        }
        File dotFile = new File(World.get().getOptions().getOutputDir(), fileName);
        logger.info("Dumping ICFG to {}", dotFile.getAbsolutePath());
        Indexer<Stmt> indexer = new SimpleIndexer<>();
        new DotDumper<Stmt>()
                .setNodeToString(n -> Integer.toString(indexer.getIndex(n)))
                .setNodeLabeler(n -> toLabel(n, icfg))
                .setGlobalNodeAttributes(DotAttributes.of("shape", "box",
                        "style", "filled", "color", "\".3 .2 1.0\""))
                .setEdgeAttributer(e -> {
                    if (e instanceof CallEdge) {
                        return DotAttributes.of("style", "dashed", "color", "blue");
                    } else if (e instanceof ReturnEdge) {
                        return DotAttributes.of("style", "dashed", "color", "red");
                    } else if (e instanceof CallToReturnEdge) {
                        return DotAttributes.of("style", "dashed");
                    } else {
                        return DotAttributes.of();
                    }
                })
                .dump(icfg, dotFile);
    }

    private static String toLabel(Stmt stmt, ICFG<JMethod, Stmt> icfg) {
        JMethod method = icfg.getContainingMethodOf(stmt);
        CFG<Stmt> cfg = getCFGOf(method);
        return CFGDumper.toLabel(stmt, cfg);
    }

    static CFG<Stmt> getCFGOf(JMethod method) {
        return method.getIR().getResult(CFGBuilder.ID);
    }
}
