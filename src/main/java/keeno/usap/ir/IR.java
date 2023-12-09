

package keeno.usap.ir;

import keeno.usap.ir.exp.Var;
import keeno.usap.ir.proginfo.ExceptionEntry;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.Indexer;
import keeno.usap.util.ResultHolder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Intermediate representation for method body of non-abstract methods.
 * Each IR contains the variables and statements defined in a method.
 */
public interface IR extends Iterable<Stmt>, Indexer<Stmt>,
        ResultHolder, Serializable {

    /**
     * @return the method that defines the content of this IR.
     */
    JMethod getMethod();

    /**
     * @return the "this" variable in this IR.
     * If the method is static, then returns null.
     */
    @Nullable
    Var getThis();

    /**
     * @return the parameters in this IR ("this" variable is excluded).
     * The order of the parameters in the resulting list is the same as
     * the order they are declared in the method.
     */
    List<Var> getParams();

    /**
     * @return the i-th parameter in this IR. The indexes start from 0.
     */
    Var getParam(int i);

    /**
     * @return {@code true} if {@code var} is a parameter of this IR.
     */
    boolean isParam(Var var);

    /**
     * @return {@code true} if {@code var} is "this" variable or a parameter
     * of this IR.
     */
    boolean isThisOrParam(Var var);

    /**
     * @return all returned variables. If the method return type is void,
     * then returns empty list.
     */
    List<Var> getReturnVars();

    /**
     * @return the i-th {@link Var} in this IR. The indexes start from 0.
     */
    Var getVar(int i);

    /**
     * @return the variables in this IR.
     */
    List<Var> getVars();

    /**
     * @return an indexer for the variables in this IR.
     */
    Indexer<Var> getVarIndexer();

    /**
     * @return the i-th {@link Stmt} in this IR. The indexes start from 0.
     */
    Stmt getStmt(int i);

    /**
     * @return a list of Stmts in this IR.
     */
    List<Stmt> getStmts();

    /**
     * @return a stream of Stmts in this IR.
     */
    default Stream<Stmt> stmts() {
        return getStmts().stream();
    }

    /**
     * Convenient method to obtain Invokes in this IR.
     *
     * @param includeIndy whether include invokedynamic in the result.
     * @return a stream of Invokes in this IR.
     */
    default Stream<Invoke> invokes(boolean includeIndy) {
        return stmts()
                .filter(s -> s instanceof Invoke)
                .map(s -> (Invoke) s)
                .filter(i -> includeIndy || !i.isDynamic());
    }

    /**
     * @return iterator of Stmts in this IR.
     */
    @Override
    default Iterator<Stmt> iterator() {
        return getStmts().iterator();
    }

    /**
     * @return the exception entries in this IR.
     * @see ExceptionEntry
     */
    List<ExceptionEntry> getExceptionEntries();
}
