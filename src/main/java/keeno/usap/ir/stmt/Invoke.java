

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.InvokeDynamic;
import keeno.usap.ir.exp.InvokeExp;
import keeno.usap.ir.exp.InvokeInstanceExp;
import keeno.usap.ir.exp.InvokeInterface;
import keeno.usap.ir.exp.InvokeSpecial;
import keeno.usap.ir.exp.InvokeStatic;
import keeno.usap.ir.exp.InvokeVirtual;
import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.RValue;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.ArraySet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/**
 * Representation of invocation statement, e.g., r = o.m(...) or o.m(...).
 */
public class Invoke extends DefinitionStmt<Var, InvokeExp>
        implements Comparable<Invoke> {

    /**
     * The variable receiving the result of the invocation. This field
     * is null if no variable receives the invocation result, e.g., o.m(...).
     */
    @Nullable
    private final Var result;

    /**
     * The invocation expression.
     */
    private final InvokeExp invokeExp;

    /**
     * The method containing this statement.
     */
    private final JMethod container;

    public Invoke(JMethod container, InvokeExp invokeExp, @Nullable Var result) {
        this.invokeExp = invokeExp;
        this.result = result;
        if (invokeExp instanceof InvokeInstanceExp) {
            Var base = ((InvokeInstanceExp) invokeExp).getBase();
            base.addInvoke(this);
        }
        this.container = container;
    }

    public Invoke(JMethod container, InvokeExp invokeExp) {
        this(container, invokeExp, null);
    }

    @Override
    @Nullable
    public Var getLValue() {
        return result;
    }

    @Nullable
    public Var getResult() {
        return result;
    }

    @Override
    public InvokeExp getRValue() {
        return invokeExp;
    }

    /**
     * @return the invocation expression of this invoke.
     * @see InvokeExp
     */
    public InvokeExp getInvokeExp() {
        return invokeExp;
    }

    /**
     * @return the method reference of this invoke.
     * @see MethodRef
     */
    public MethodRef getMethodRef() {
        return invokeExp.getMethodRef();
    }

    public boolean isVirtual() {
        return invokeExp instanceof InvokeVirtual;
    }

    public boolean isInterface() {
        return invokeExp instanceof InvokeInterface;
    }

    public boolean isSpecial() {
        return invokeExp instanceof InvokeSpecial;
    }

    public boolean isStatic() {
        return invokeExp instanceof InvokeStatic;
    }

    public boolean isDynamic() {
        return invokeExp instanceof InvokeDynamic;
    }

    public JMethod getContainer() {
        return container;
    }

    @Override
    public Optional<LValue> getDef() {
        return Optional.ofNullable(result);
    }

    @Override
    public Set<RValue> getUses() {
        Set<RValue> uses = new ArraySet<>(invokeExp.getUses());
        uses.add(invokeExp);
        return uses;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    private static final Comparator<Invoke> COMPARATOR =
            Comparator.comparing((Invoke invoke) ->
                            invoke.getContainer().getDeclaringClass().toString())
                    .thenComparingInt(Stmt::getLineNumber)
                    .thenComparing(invoke -> invoke.getContainer().toString())
                    .thenComparingInt(Stmt::getIndex);

    @Override
    public int compareTo(@Nonnull Invoke other) {
        return COMPARATOR.compare(this, other);
    }

    @Override
    public String toString() {
        String ret = result == null ? "" : result + " = ";
        return String.format("%s[%d@L%d] %s%s",
                container, getIndex(), getLineNumber(), ret, invokeExp);
    }
}
