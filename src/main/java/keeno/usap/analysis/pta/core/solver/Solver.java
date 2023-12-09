

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.analysis.pta.core.cs.selector.ContextSelector;
import keeno.usap.analysis.pta.core.heap.HeapModel;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.plugin.Plugin;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.config.AnalysisOptions;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.language.type.TypeSystem;

import java.util.Collection;
import java.util.function.Predicate;

public interface Solver {

    AnalysisOptions getOptions();

    ClassHierarchy getHierarchy();

    TypeSystem getTypeSystem();

    HeapModel getHeapModel();

    CSManager getCSManager();

    ContextSelector getContextSelector();

    CallGraph<CSCallSite, CSMethod> getCallGraph();

    /**
     * Returns the points-to set of given pointer. If the pointer has not
     * been associated with a points-to set, this method will create a
     * new set and associate it with the pointer.
     */
    PointsToSet getPointsToSetOf(Pointer pointer);

    /**
     * Creates a new empty points-to set.
     */
    PointsToSet makePointsToSet();

    /**
     * Sets plugin to this solver.
     */
    void setPlugin(Plugin plugin);

    /**
     * Starts this solver.
     */
    void solve();

    // ---------- side-effect APIs (begin) ----------
    // These side-effect APIs could be used by Plugins to update
    // points-to information.

    // APIs for adding points-to relations
    void addPointsTo(Pointer pointer, PointsToSet pts);

    void addPointsTo(Pointer pointer, CSObj csObj);

    void addPointsTo(Pointer pointer, Context heapContext, Obj obj);

    /**
     * Convenient API to add points-to relation for object
     * with empty heap context.
     */
    default void addPointsTo(Pointer pointer, Obj obj) {
        addPointsTo(pointer, getContextSelector().getEmptyContext(), obj);
    }

    // convenient APIs for adding var-points-to relations
    void addVarPointsTo(Context context, Var var, PointsToSet pts);

    void addVarPointsTo(Context context, Var var, CSObj csObj);

    void addVarPointsTo(Context context, Var var, Context heapContext, Obj obj);

    /**
     * Convenient API to add var points-to relation for object
     * with empty heap context.
     */
    default void addVarPointsTo(Context context, Var var, Obj obj) {
        addVarPointsTo(context, var, getContextSelector().getEmptyContext(), obj);
    }

    /**
     * Adds an object filter to given pointer.
     * Note that the filter works only after it is added to the pointer,
     * and it cannot filter out the objects pointed to by the pointer
     * before it is added.
     */
    void addPointerFilter(Pointer pointer, Predicate<CSObj> filter);

    /**
     * Adds an edge "source -> target" to the PFG.
     */
    default void addPFGEdge(Pointer source, Pointer target, FlowKind kind) {
        addPFGEdge(source, target, kind, Identity.get());
    }

    /**
     * Adds an edge "source -> target" to the PFG.
     * For the objects pointed to by "source", only the ones whose types
     * are subtypes of given type are propagated to "target".
     */
    default void addPFGEdge(Pointer source, Pointer target, FlowKind kind, Type type) {
        addPFGEdge(source, target, kind, new TypeFilter(type, this));
    }

    /**
     * Adds an edge "source -> target" (with edge transfer) to the PFG.
     */
    void addPFGEdge(Pointer source, Pointer target, FlowKind kind, Transfer transfer);

    /**
     * Adds an entry point.
     * Notes that the method in entry point will be set as an entry in {@link CallGraph}
     */
    void addEntryPoint(EntryPoint entryPoint);

    /**
     * Adds a call edge.
     *
     * @param edge the added edge.
     */
    void addCallEdge(Edge<CSCallSite, CSMethod> edge);

    /**
     * Adds a context-sensitive method.
     *
     * @param csMethod the added context-sensitive method.
     */
    void addCSMethod(CSMethod csMethod);

    /**
     * Adds stmts to the analyzed program. Solver will process given stmts.
     *
     * @param csMethod the container method of the stmts
     * @param stmts    the added stmts
     */
    void addStmts(CSMethod csMethod, Collection<Stmt> stmts);

    /**
     * If a plugin takes over the analysis of a method, and wants this solver
     * to ignore the method (for precision and/or efficiency reasons),
     * then it could call this API with the method.
     * After that, this solver will not process the method body.
     * <p>
     * Typically, this API should be called at the initial stage of
     * pointer analysis, i.e., in {@link Plugin#onStart()}.
     *
     * @param method the method to be ignored.
     */
    void addIgnoredMethod(JMethod method);

    /**
     * Analyzes the static initializer (i.e., &lt;clinit&gt;) of given class.
     *
     * @param cls the class to be initialized.
     */
    void initializeClass(JClass cls);
    // ---------- side-effect APIs (end) ----------

    /**
     * @return pointer analysis result.
     */
    PointerAnalysisResult getResult();
}
