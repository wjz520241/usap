

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.core.solver.Solver;

/**
 * Abstract class for taint analysis handlers.
 */
abstract class Handler {

    protected final Solver solver;

    protected final TaintManager manager;

    protected final boolean callSiteMode;

    protected Handler(HandlerContext context) {
        solver = context.solver();
        manager = context.manager();
        callSiteMode = context.config().callSiteMode();
    }
}
