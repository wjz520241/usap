

package keeno.usap.analysis.pta.core.cs.element;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.util.Indexable;

/**
 * Represents context-sensitive objects.
 */
public class CSObj extends AbstractCSElement implements Indexable {

    private final Obj obj;

    private final int index;

    CSObj(Obj obj, Context context, int index) {
        super(context);
        this.obj = obj;
        this.index = index;
    }

    /**
     * @return the abstract object (without context).
     */
    public Obj getObject() {
        return obj;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return context + ":" + obj;
    }
}
