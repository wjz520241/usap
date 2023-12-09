

package keeno.usap.analysis.pta.plugin.natives;

import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.Plugin;
import keeno.usap.analysis.pta.plugin.util.IRModel;
import keeno.usap.language.classes.JMethod;

import java.util.List;

/**
 * This class models some native calls by "inlining" their side effects
 * at the call sites to provide better precision for pointer analysis.
 */
public class NativeModeller implements Plugin {

    private Solver solver;

    private List<IRModel> models;

    private DoPriviledgedModel doPrivilegedModel;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        doPrivilegedModel = new DoPriviledgedModel(solver);
        models = List.of(doPrivilegedModel,
                new ArrayModel(solver),
                new UnsafeModel(solver));
    }

    @Override
    public void onStart() {
        models.forEach(model ->
                model.getModeledAPIs().forEach(solver::addIgnoredMethod));
    }

    @Override
    public void onNewMethod(JMethod method) {
        models.forEach(model -> model.handleNewMethod(method));
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        models.forEach(model -> model.handleNewCSMethod(csMethod));
    }

    @Override
    public void onNewCallEdge(Edge<CSCallSite, CSMethod> edge) {
        doPrivilegedModel.handleNewCallEdge(edge);
    }
}
