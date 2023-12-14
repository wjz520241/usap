

package keeno.usap.analysis.pta.core.cs.context;

import keeno.usap.util.collection.Maps;

import java.util.Arrays;
import java.util.Map;

/**
 * An implementation of {@link Context}, which organizes contexts as Trie.
 */
public class TrieContext implements Context {

    private final TrieContext parent;

    private final Object elem;

    private final int length;

    private Map<Object, TrieContext> children;

    private TrieContext() {
        parent = null;
        elem = null;
        length = 0;
    }

    private TrieContext(TrieContext parent, Object elem) {
        this.parent = parent;
        this.elem = elem;
        this.length = parent.getLength() + 1;
    }

    @Override
    public int getLength() {
        return length;
    }

    /**
     *一个巧妙的递归设计，不会访问最初的默认上下文
     */
    @Override
    public Object getElementAt(int i) {
        assert 0 <= i && i < length;
        if (i == length - 1) {
            return elem;
        } else {
            return parent.getElementAt(i);
        }
    }

    TrieContext getParent() {
        return parent;
    }

    TrieContext getChild(Object elem) {
        if (children == null) {
            children = Maps.newHybridMap();
        }
        return children.computeIfAbsent(elem,
                e -> new TrieContext(this, e));
    }

    Object getElem() {
        return elem;
    }

    @Override
    public String toString() {
        Object[] elems = new Object[length];
        TrieContext c = this;
        for (int i = length - 1; i >= 0; --i) {
            elems[i] = c.getElem();
            c = c.getParent();
        }
        return Arrays.toString(elems);
    }

    public static class Factory<T> implements ContextFactory<T> {

        /**
         * 此工厂生成的所有树上下文的根上下文。它还充当默认上下文。
         */
        private final TrieContext rootContext = new TrieContext();

        @Override
        public TrieContext getEmptyContext() {
            return rootContext;
        }

        @Override
        public Context make(T elem) {
            return rootContext.getChild(elem);
        }

        @Override
        public TrieContext make(T... elems) {
            TrieContext result = rootContext;
            for (T elem : elems) {
                result = result.getChild(elem);
            }
            return result;
        }

        @Override
        public TrieContext makeLastK(Context context, int k) {
            if (k == 0) {
                return rootContext;
            }
            TrieContext c = (TrieContext) context;
            if (c.getLength() <= k) {
                return c;
            }
            Object[] elems = new Object[k];
            for (int i = k; i > 0; --i) {
                elems[i - 1] = c.getElem();
                c = c.getParent();
            }
            return make((T[]) elems);
        }

        @Override
        public TrieContext append(Context parent, T elem, int limit) {
            TrieContext p = (TrieContext) parent;
            if (parent.getLength() < limit) {
                return p.getChild(elem);
            } else {
                return makeLastK(p, limit - 1).getChild(elem);
            }
        }
    }
}
