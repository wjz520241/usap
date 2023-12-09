

package keeno.usap.util.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UnionFindSet<E> {

    /**
     * Associates elements to UF set entries.
     */
    private final Map<E, Entry> entries = new HashMap<>();

    /**
     * Number of disjoint sets.
     */
    private int setCount;

    public UnionFindSet(Collection<E> elems) {
        elems.forEach(elem -> entries.put(elem, new Entry(elem)));
        setCount = entries.size();
    }

    /**
     * Unions the sets which e1 and e2 belong to, respectively.
     *
     * @return {@code true} if this union-find set changed as a result
     * of this call.
     */
    public boolean union(E e1, E e2) {
        Entry root1 = findRootEntry(entries.get(e1));
        Entry root2 = findRootEntry(entries.get(e2));
        if (root1 == root2) {
            return false;
        } else { // union by rank
            if (root1.rank < root2.rank) {
                root1.parent = root2;
            } else if (root1.rank > root2.rank) {
                root2.parent = root1;
            } else {
                root2.parent = root1;
                ++root2.rank;
            }
            --setCount;
            return true;
        }
    }

    /**
     * @return {@code true} if e1 and e2 belong to the same set.
     */
    public boolean isConnected(E e1, E e2) {
        Entry root1 = findRootEntry(entries.get(e1));
        Entry root2 = findRootEntry(entries.get(e2));
        return root1 == root2;
    }

    /**
     * @return the root element of the set which e belongs to.
     */
    public E findRoot(E e) {
        return findRootEntry(entries.get(e)).elem;
    }

    /**
     * @return number of disjoint sets in this union-find set.
     */
    public int numberOfSets() {
        return setCount;
    }

    /**
     * @return a collection of all disjoint sets in this union-find set.
     */
    public Collection<Set<E>> getDisjointSets() {
        return entries.keySet()
                .stream()
                .collect(Collectors.groupingBy(this::findRoot, Collectors.toSet()))
                .values();
    }

    private Entry findRootEntry(Entry ent) {
        if (ent.parent != ent) { // path compression
            ent.parent = findRootEntry(ent.parent);
        }
        return ent.parent;
    }

    private class Entry {

        private final E elem;
        private Entry parent;
        private int rank;

        private Entry(E elem) {
            this.elem = elem;
            this.parent = this;
            this.rank = 0;
        }
    }
}
