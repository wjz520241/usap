

package keeno.usap.analysis.pta.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.analysis.ProgramAnalysis;
import keeno.usap.analysis.StmtResult;
import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.pta.PointerAnalysis;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.Sets;

import java.util.Set;

/**
 * Collects statements in program that the client wants.
 */
abstract class Collector extends ProgramAnalysis<StmtResult<Boolean>> {

    private static final Logger logger = LogManager.getLogger(Collector.class);

    Collector(AnalysisConfig config) {
        super(config);
    }

    @Override
    public StmtResult<Boolean> analyze() {
        PointerAnalysisResult result = World.get().getResult(PointerAnalysis.ID);
        CallGraph<Invoke, JMethod> callGraph = result.getCallGraph();
        Set<Stmt> wantedStmts = Sets.newSet();
        int nRelevantStmts = 0;
        int nWantedAppStmts = 0, nRelevantAppStmts = 0;
        // collect want statements and count
        for (JMethod method : callGraph) {
            boolean isApp = method.isApplication();
            for (Stmt stmt : method.getIR()) {
                if (isRelevant(stmt)) {
                    ++nRelevantStmts;
                    if (isApp) {
                        ++nRelevantAppStmts;
                    }
                    if (isWanted(stmt, result)) {
                        wantedStmts.add(stmt);
                        if (isApp) {
                            ++nWantedAppStmts;
                        }
                    }
                }
            }
        }
        // log statistics
        logger.info("#{}: found {} in {} reachable relevant Stmts",
                getDescription(), wantedStmts.size(), nRelevantStmts);
        logger.info("#{}: found {} in {} reachable relevant Stmts (app)",
                getDescription(), nWantedAppStmts, nRelevantAppStmts);
        // convert result to StmtResult
        return new StmtResult<>() {

            @Override
            public boolean isRelevant(Stmt stmt) {
                return Collector.this.isRelevant(stmt);
            }

            @Override
            public Boolean getResult(Stmt stmt) {
                return wantedStmts.contains(stmt);
            }
        };
    }

    /**
     * @return {@code true} if the given statement is relevant to the client.
     */
    abstract boolean isRelevant(Stmt stmt);

    /**
     * @return {@code true} if the given statement is wanted by the client.
     */
    abstract boolean isWanted(Stmt stmt, PointerAnalysisResult result);

    /**
     * @return description of wanted statements
     */
    abstract String getDescription();
}
