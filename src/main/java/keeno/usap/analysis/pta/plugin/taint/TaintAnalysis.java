

package keeno.usap.analysis.pta.plugin.taint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.CompositePlugin;
import keeno.usap.analysis.pta.plugin.Plugin;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.Timer;

import java.io.File;
import java.util.Set;

public class TaintAnalysis implements Plugin {

    private static final Logger logger = LogManager.getLogger(TaintAnalysis.class);

    private static final String TAINT_FLOW_GRAPH_FILE = "taint-flow-graph.dot";

    private Solver solver;

    private TaintManager manager;

    private Plugin onFlyHandler;

    private SinkHandler sinkHandler;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        manager = new TaintManager(solver.getHeapModel());
        TaintConfig config = TaintConfig.loadConfig(
                solver.getOptions().getString("taint-config"),
                solver.getHierarchy(),
                solver.getTypeSystem());
        logger.info(config);
        HandlerContext context = new HandlerContext(solver, manager, config);
        CompositePlugin onFlyHandler = new CompositePlugin();
        onFlyHandler.addPlugin(
                new SourceHandler(context),
                new TransferHandler(context),
                new SanitizerHandler(context));
        this.onFlyHandler = onFlyHandler;
        sinkHandler = new SinkHandler(context);
    }

    @Override
    public void onNewCallEdge(Edge<CSCallSite, CSMethod> edge) {
        onFlyHandler.onNewCallEdge(edge);
    }

    @Override
    public void onNewStmt(Stmt stmt, JMethod container) {
        onFlyHandler.onNewStmt(stmt, container);
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        onFlyHandler.onNewCSMethod(csMethod);
    }

    @Override
    public void onNewPointsToSet(CSVar csVar, PointsToSet pts) {
        onFlyHandler.onNewPointsToSet(csVar, pts);
    }

    /**
     * 收集制作污点流数据
     */
    @Override
    public void onFinish() {
        Set<TaintFlow> taintFlows = sinkHandler.collectTaintFlows();
        solver.getResult().storeResult(getClass().getName(), taintFlows);
        logger.info("Detected {} taint flow(s):", taintFlows.size());
        taintFlows.forEach(logger::info);
        Timer.runAndCount(() -> new TFGDumper().dump(
                        new TFGBuilder(solver.getResult(), taintFlows, manager).build(),
                        new File(World.get().getOptions().getOutputDir(), TAINT_FLOW_GRAPH_FILE)),
                "TFGDumper");
    }
}
