

package keeno.usap.analysis.pta;

import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.graph.flowgraph.ObjectFlowGraph;
import keeno.usap.analysis.pta.core.cs.element.ArrayIndex;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.cs.element.InstanceField;
import keeno.usap.analysis.pta.core.cs.element.StaticField;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.ir.exp.ArrayAccess;
import keeno.usap.ir.exp.FieldAccess;
import keeno.usap.ir.exp.InstanceFieldAccess;
import keeno.usap.ir.exp.StaticFieldAccess;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.Indexer;
import keeno.usap.util.ResultHolder;

import java.util.Collection;
import java.util.Set;

/**
 * Represents results of pointer analysis.
 * This class provides various API for querying points-to sets of
 * different kinds of pointer-accessing expressions. For the expressions
 * that are ignored by pointer analysis, an empty set is returned.
 */
public interface PointerAnalysisResult extends ResultHolder {

    /**
     * @return all reachable context-sensitive variables in the program.
     */
    Collection<CSVar> getCSVars();

    /**
     * @return all reachable variables in the program.
     */
    Collection<Var> getVars();

    /**
     * @return all reachable instance fields in the program.
     */
    Collection<InstanceField> getInstanceFields();

    /**
     * @return all reachable array indexes in the program.
     */
    Collection<ArrayIndex> getArrayIndexes();

    /**
     * @return all reachable static fields in the program.
     */
    Collection<StaticField> getStaticFields();

    /**
     * @return all reachable context-sensitive objects in the program.
     */
    Collection<CSObj> getCSObjects();

    /**
     * @return all reachable objects in the program.
     */
    Collection<Obj> getObjects();

    /**
     * @return indexer for Obj in the program.
     */
    Indexer<Obj> getObjectIndexer();

    /**
     * @return set of Obj pointed to by var.
     */
    Set<Obj> getPointsToSet(Var var);

    /**
     * @return set of Obj pointed to by field access.
     */
    default Set<Obj> getPointsToSet(FieldAccess access) {
        if (access instanceof InstanceFieldAccess ifaccess) {
            return getPointsToSet(ifaccess);
        } else {
            return getPointsToSet((StaticFieldAccess) access);
        }
    }

    /**
     * @return set of Obj pointed to by given instance field access, e.g., o.f.
     */
    Set<Obj> getPointsToSet(InstanceFieldAccess access);

    /**
     * @return set of Obj pointed to by base.field.
     */
    Set<Obj> getPointsToSet(Var base, JField field);

    /**
     * @return set of Obj pointed to by in given base.field.
     */
    Set<Obj> getPointsToSet(Obj base, JField field);

    /**
     * @return set of Obj pointed to by given static field access, e.g., T.f.
     */
    Set<Obj> getPointsToSet(StaticFieldAccess access);

    /**
     * @return points-to set of given field. The field is supposed to be static.
     */
    Set<Obj> getPointsToSet(JField field);

    /**
     * @return set of Obj pointed to by given array access, e.g., a[i].
     */
    Set<Obj> getPointsToSet(ArrayAccess access);

    /**
     * @return points-to set of given array index.
     * The base is supposed to be of array type; parameter index is unused.
     */
    Set<Obj> getPointsToSet(Var base, Var index);

    /**
     * @return set of Obj pointed to by given array.
     */
    Set<Obj> getPointsToSet(Obj array);

    /**
     * @return {@code true} if two variables may be aliases.
     */
    boolean mayAlias(Var v1, Var v2);

    /**
     * @return {@code true} if two instance field accesses may be aliases.
     */
    boolean mayAlias(InstanceFieldAccess if1, InstanceFieldAccess if2);

    /**
     * @return {@code true} if two array accesses may be aliases.
     */
    boolean mayAlias(ArrayAccess a1, ArrayAccess a2);

    /**
     * @return the resulting context-sensitive call graph.
     */
    CallGraph<CSCallSite, CSMethod> getCSCallGraph();

    /**
     * @return the resulting call graph (without contexts).
     */
    CallGraph<Invoke, JMethod> getCallGraph();

    /**
     * @return the resulting object flow graph.
     */
    ObjectFlowGraph getObjectFlowGraph();
}
