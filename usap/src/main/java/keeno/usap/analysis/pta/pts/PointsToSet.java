

package keeno.usap.analysis.pta.pts;

import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.util.Copyable;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Representation of points-to sets that consist of {@link CSObj}.
 */
public interface PointsToSet extends Iterable<CSObj>, Copyable<PointsToSet> {

    /**
     * Adds an object to this set.
     *
     * @return true if this points-to set changed as a result of the call,
     * otherwise false.
     */
    boolean addObject(CSObj obj);

    /**
     * Adds all objects in given pts to this set.
     *
     * @return true if this points-to set changed as a result of the call,
     * otherwise false.
     */
    boolean addAll(PointsToSet pts);

    /**
     * Adds all objects in given pts to this set.
     *
     * @return the difference between {@code pts} and this set.
     */
    PointsToSet addAllDiff(PointsToSet pts);

    /**
     * @return true if this set contains given object, otherwise false.
     */
    boolean contains(CSObj obj);

    /**
     * @return whether this set if empty.
     */
    boolean isEmpty();

    /**
     * @return the number of objects in this set.
     */
    int size();

    /**
     * @return all objects in this set.
     */
    Set<CSObj> getObjects();

    /**
     * @return all objects in this set.
     */
    Stream<CSObj> objects();

    @Override
    default Iterator<CSObj> iterator() {
        return getObjects().iterator();
    }
}
