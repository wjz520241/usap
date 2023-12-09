

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.core.solver.Solver;

/**
 * Contains information used by taint analysis handlers.
 */
record HandlerContext(Solver solver,
                      TaintManager manager,
                      TaintConfig config) {
}
