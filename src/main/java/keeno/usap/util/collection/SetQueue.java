

package keeno.usap.util.collection;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A Queue implementation which contains no duplicate elements.
 *
 * @param <E> type of elements.
 */
public class SetQueue<E> extends AbstractQueue<E> implements Serializable {

    private final Set<E> set = new LinkedHashSet<>();

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean add(E e) {
        return set.add(e);
    }

    @Override
    public boolean offer(E e) {
        return set.add(e);
    }

    @Override
    public E poll() {
        Iterator<E> it = set.iterator();
        if (it.hasNext()) {
            E e = it.next();
            it.remove();
            return e;
        } else {
            return null;
        }
    }

    @Override
    public E peek() {
        Iterator<E> it = set.iterator();
        return it.hasNext() ? it.next() : null;
    }
}
