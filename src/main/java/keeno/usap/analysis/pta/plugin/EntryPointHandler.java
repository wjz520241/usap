

package keeno.usap.analysis.pta.plugin;

import keeno.usap.World;
import keeno.usap.analysis.pta.core.solver.DeclaredParamProvider;
import keeno.usap.analysis.pta.core.solver.EmptyParamProvider;
import keeno.usap.analysis.pta.core.solver.EntryPoint;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.language.classes.JMethod;

/**
 * Initializes standard entry points for pointer analysis.
 */
public class EntryPointHandler implements Plugin {

    private Solver solver;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void onStart() {
        // process program main method
        JMethod main = World.get().getMainMethod();
        if (main != null) {
            solver.addEntryPoint(new EntryPoint(main,
                    new DeclaredParamProvider(main, solver.getHeapModel(), 1)));
        }
        // process implicit entries
        if (solver.getOptions().getBoolean("implicit-entries")) {
            for (JMethod entry : World.get().getImplicitEntries()) {
                solver.addEntryPoint(new EntryPoint(entry, EmptyParamProvider.get()));
            }
        }
    }
}
