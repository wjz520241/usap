

package keeno.usap.analysis.pta.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.Sets;

import java.util.Set;

/**
 * This class is for debugging/testing purpose.
 * <p>
 * {@link keeno.usap.analysis.pta.core.solver.Solver} needs to satisfy
 * some important constraints:
 * <ol>
 *     <li>{@code onNewMethod(m)} must happen before {@code onNewCSMethod(csM)}
 *     for any context-sensitive method {@code csM} for m.</li>
 *     <li>{@code onNewMethod(m)} must happen before {@code onNewPointsToSet(csV, pts)}
 *     for any context-sensitive variable {@code csV} in {@code m}, and</li>
 *     <li>{@code onNewCSMethod(csM)} must happen before {@code onNewPointsToSet(csV, pts)}
 *     for any context-sensitive variable {@code csV} in {@code csM}.</li>
 * </ol>
 *
 * <p>This class checks the constraints and issues warnings when they are unsatisfied.
 */
public class ConstraintChecker implements Plugin {

    private static final Logger logger = LogManager.getLogger(ConstraintChecker.class);

    private final Set<JMethod> reached = Sets.newSet(4096);

    private final Set<CSMethod> reachedCS = Sets.newSet(8192);

    private CSManager csManager;

    @Override
    public void setSolver(Solver solver) {
        csManager = solver.getCSManager();
    }

    @Override
    public void onNewMethod(JMethod method) {
        reached.add(method);
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        if (!reached.contains(csMethod.getMethod())) {
            logger.warn("Warning: hit {} before processing {}",
                    csMethod, csMethod.getMethod());
        }
        reachedCS.add(csMethod);
    }

    @Override
    public void onNewPointsToSet(CSVar csVar, PointsToSet pts) {
        Var var = csVar.getVar();
        JMethod method = var.getMethod();
        if (!reached.contains(method)) {
            logger.warn("Warning: hit {} before processing {}", var, method);
        }
        CSMethod csMethod = csManager.getCSMethod(csVar.getContext(), method);
        if (!reachedCS.contains(csMethod)) {
            logger.warn("Warning: hit {} before processing {}", csVar, csMethod);
        }
    }
}
