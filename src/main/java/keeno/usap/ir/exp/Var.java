

package keeno.usap.ir.exp;

import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.LoadArray;
import keeno.usap.ir.stmt.LoadField;
import keeno.usap.ir.stmt.StoreArray;
import keeno.usap.ir.stmt.StoreField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.AnalysisException;
import keeno.usap.util.Indexable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 方法构造函数参数、lambda参数、异常参数和局部变量的表示
 */
public class Var implements LValue, RValue, Indexable {

    /**
     * The method containing this Var.
     */
    private final JMethod method;

    /**
     * The name of this Var.
     */
    private final String name;

    /**
     * The type of this Var.
     */
    private final Type type;

    /**
     * The index of this variable in {@link #method}.
     */
    private final int index;

    /**
     * If this variable is a (temporary) variable generated for holding
     * a constant value, then this field holds that constant value;
     * otherwise, this field is null.
     */
    private final Literal constValue;

    /**
     * Relevant statements of this variable.
     * <br>
     * Notes: add {@code transient} to control (de)serialization to
     * avoid the inequality of {@link RelevantStmts#EMPTY}.
     *
     * @see #writeObject(ObjectOutputStream)
     * @see #readObject(ObjectInputStream)
     */
    private transient RelevantStmts relevantStmts = RelevantStmts.EMPTY;

    public Var(JMethod method, String name, Type type, int index) {
        this(method, name, type, index, null);
    }

    public Var(JMethod method, String name, Type type, int index,
               @Nullable Literal constValue) {
        this.method = method;
        this.name = name;
        this.type = type;
        this.index = index;
        this.constValue = constValue;
    }

    /**
     * @return the method containing this Var.
     */
    public JMethod getMethod() {
        return method;
    }

    /**
     * @return the index of this variable in the container IR.
     */
    @Override
    public int getIndex() {
        return index;
    }

    /**
     * @return name of this Var.
     */
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    /**
     * @return true if this variable is a (temporary) variable
     * generated for holding constant value, otherwise false.
     */
    public boolean isConst() {
        return constValue != null;
    }

    /**
     * @return the constant value held by this variable.
     * @throws AnalysisException if this variable does not hold const value
     */
    public Literal getConstValue() {
        if (!isConst()) {
            throw new AnalysisException(this
                    + " is not a (temporary) variable for holding const value");
        }
        return constValue;
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }

    public void addLoadField(LoadField loadField) {
        ensureRelevantStmts();
        relevantStmts.addLoadField(loadField);
    }

    /**
     * @return {@link LoadField}s whose base variable is this Var.
     */
    public List<LoadField> getLoadFields() {
        return relevantStmts.getLoadFields();
    }

    public void addStoreField(StoreField storeField) {
        ensureRelevantStmts();
        relevantStmts.addStoreField(storeField);
    }

    /**
     * @return {@link StoreField}s whose base variable is this Var.
     */
    public List<StoreField> getStoreFields() {
        return relevantStmts.getStoreFields();
    }

    public void addLoadArray(LoadArray loadArray) {
        ensureRelevantStmts();
        relevantStmts.addLoadArray(loadArray);
    }

    /**
     * @return {@link LoadArray}s whose base variable is this Var.
     */
    public List<LoadArray> getLoadArrays() {
        return relevantStmts.getLoadArrays();
    }

    public void addStoreArray(StoreArray storeArray) {
        ensureRelevantStmts();
        relevantStmts.addStoreArray(storeArray);
    }

    /**
     * @return {@link StoreArray}s whose base variable is this Var.
     */
    public List<StoreArray> getStoreArrays() {
        return relevantStmts.getStoreArrays();
    }

    public void addInvoke(Invoke invoke) {
        ensureRelevantStmts();
        relevantStmts.addInvoke(invoke);
    }

    /**
     * @return {@link Invoke}s whose base variable is this Var.
     */
    public List<Invoke> getInvokes() {
        return relevantStmts.getInvokes();
    }

    /**
     * Ensure {@link #relevantStmts} points to an instance other than
     * {@link RelevantStmts#EMPTY}.
     */
    private void ensureRelevantStmts() {
        if (relevantStmts == RelevantStmts.EMPTY) {
            relevantStmts = new RelevantStmts();
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (relevantStmts == RelevantStmts.EMPTY) {
            s.writeObject(null);
        } else {
            s.writeObject(relevantStmts);
        }
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        relevantStmts = (RelevantStmts) s.readObject();
        if (relevantStmts == null) {
            relevantStmts = RelevantStmts.EMPTY;
        }
    }

    /**
     * Relevant statements of a variable, say v, which include:
     * load field: x = v.f;
     * store field: v.f = x;
     * load array: x = v[i];
     * store array: v[i] = x;
     * invocation: v.f();
     * We use a separate class to store these relevant statements
     * (instead of directly storing them in {@link Var}) for saving space.
     * Most variables do not have any relevant statements, so these variables
     * only need to hold one reference to the empty {@link RelevantStmts},
     * instead of several references to empty lists.
     */
    private static class RelevantStmts implements Serializable {

        private static final RelevantStmts EMPTY = new RelevantStmts();

        private static final int DEFAULT_CAPACITY = 4;

        // Contract: if the following fields are empty, they must point to
        // Collections.emptyList();
        private List<LoadField> loadFields = List.of();
        private List<StoreField> storeFields = List.of();
        private List<LoadArray> loadArrays = List.of();
        private List<StoreArray> storeArrays = List.of();
        private List<Invoke> invokes = List.of();

        private List<LoadField> getLoadFields() {
            return unmodifiable(loadFields);
        }

        private void addLoadField(LoadField loadField) {
            if (loadFields.isEmpty()) {
                loadFields = new ArrayList<>();
            }
            loadFields.add(loadField);
        }

        private List<StoreField> getStoreFields() {
            return unmodifiable(storeFields);
        }

        private void addStoreField(StoreField storeField) {
            if (storeFields.isEmpty()) {
                storeFields = new ArrayList<>(DEFAULT_CAPACITY);
            }
            storeFields.add(storeField);
        }

        private List<LoadArray> getLoadArrays() {
            return unmodifiable(loadArrays);
        }

        private void addLoadArray(LoadArray loadArray) {
            if (loadArrays.isEmpty()) {
                loadArrays = new ArrayList<>(DEFAULT_CAPACITY);
            }
            loadArrays.add(loadArray);
        }

        private List<StoreArray> getStoreArrays() {
            return unmodifiable(storeArrays);
        }

        private void addStoreArray(StoreArray storeArray) {
            if (storeArrays.isEmpty()) {
                storeArrays = new ArrayList<>(DEFAULT_CAPACITY);
            }
            storeArrays.add(storeArray);
        }

        private List<Invoke> getInvokes() {
            return unmodifiable(invokes);
        }

        private void addInvoke(Invoke invoke) {
            if (invokes.isEmpty()) {
                invokes = new ArrayList<>(DEFAULT_CAPACITY);
            }
            invokes.add(invoke);
        }

        private static <T> List<T> unmodifiable(List<T> list) {
            return list.isEmpty() ? list : Collections.unmodifiableList(list);
        }
    }
}
