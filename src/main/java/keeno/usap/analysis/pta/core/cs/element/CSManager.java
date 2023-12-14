

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.Indexer;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * 管理指针分析中上下文相关的元素和指针。
 */
public interface CSManager {

    /**
     * @return 给定上下文和变量，提供一个上下文敏感变量。
     */
    CSVar getCSVar(Context context, Var var);

    /**
     * @return 给定上下文和对象，提供一个上下文敏感对象。
     */
    CSObj getCSObj(Context heapContext, Obj obj);

    /**
     * @return 给定上下文和调用点，提供一个上下文敏感调用点。
     */
    CSCallSite getCSCallSite(Context context, Invoke callSite);

    /**
     * @return 给定上下文和方法，提供一个上下文敏感方法。
     */
    CSMethod getCSMethod(Context context, JMethod method);

    /**
     * @return the corresponding StaticField pointer for given static field.
     */
    StaticField getStaticField(JField field);

    /**
     * @return the corresponding InstanceField pointer for given object
     * and instance field.
     */
    InstanceField getInstanceField(CSObj base, JField field);

    /**
     * @return the corresponding ArrayIndex pointer for given array object.
     */
    ArrayIndex getArrayIndex(CSObj array);

    /**
     * @return all variables (without contexts).
     */
    Collection<Var> getVars();

    /**
     * @return all relevant context-sensitive variables for given variable.
     */
    Collection<CSVar> getCSVarsOf(Var var);

    /**
     * @return all context-sensitive variables.
     */
    Collection<CSVar> getCSVars();

    /**
     * @return all context-sensitive objects.
     */
    Collection<CSObj> getObjects();

    /**
     * @return all relevant context-sensitive objects for given object.
     */
    Collection<CSObj> getCSObjsOf(Obj obj);

    /**
     * @return all static field pointers.
     */
    Collection<StaticField> getStaticFields();

    /**
     * @return all instance field pointers.
     */
    Collection<InstanceField> getInstanceFields();

    /**
     * @return all array index pointers.
     */
    Collection<ArrayIndex> getArrayIndexes();

    /**
     * @return all pointers managed by this manager.
     */
    Stream<Pointer> pointers();

    /**
     * @return {@link Indexer} for {@link CSObj} maintained by this manager.
     * The indexer is useful for creating efficient points-to sets.
     */
    Indexer<CSObj> getObjectIndexer();

    /**
     * @return {@link Indexer} for {@link CSMethod} maintained by this manager.
     */
    Indexer<CSMethod> getMethodIndexer();
}
