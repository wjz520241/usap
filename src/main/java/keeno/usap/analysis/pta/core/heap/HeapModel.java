

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.ir.exp.ReferenceLiteral;
import keeno.usap.ir.stmt.New;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.Indexer;

import java.util.Collection;

/**
 * Represents of heap models for heap objects.
 * Pointer analysis should ALWAYS obtain {@link Obj} via this interface.
 *
 * @see Obj
 */
public interface HeapModel extends Indexer<Obj> {

    /**
     * @return the abstract object for given new statement.
     */
    Obj getObj(New allocSite);

    /**
     * @return the constant object for given value.
     */
    Obj getConstantObj(ReferenceLiteral value);

    /**
     * @return {@code true} if {@code obj} represents a string constant.
     */
    boolean isStringConstant(Obj obj);

    Obj getMockObj(Descriptor desc, Object alloc, Type type,
                   JMethod container, boolean isFunctional);

    /**
     * @return the mock object for given arguments.
     */
    default Obj getMockObj(Descriptor desc, Object alloc, Type type, JMethod container) {
        return getMockObj(desc, alloc, type, container, true);
    }

    /**
     * @return the mock object for given arguments.
     */
    default Obj getMockObj(Descriptor desc, Object alloc, Type type, boolean isFunctional) {
        return getMockObj(desc, alloc, type, null, isFunctional);
    }

    /**
     * @return the mock object for given arguments.
     */
    default Obj getMockObj(Descriptor desc, Object alloc, Type type) {
        return getMockObj(desc, alloc, type, null);
    }

    /**
     * @return all objects in this heap model.
     */
    Collection<Obj> getObjects();
}
