

package keeno.usap.ir;

import keeno.usap.ir.exp.Var;
import keeno.usap.ir.proginfo.ExceptionEntry;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.AbstractResultHolder;
import keeno.usap.util.Indexer;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of IR.
 * The data structures in this class are immutable.
 */
public class DefaultIR extends AbstractResultHolder implements IR {

    private final JMethod method;

    private final Var thisVar;

    private final List<Var> params;

    private final List<Var> vars;

    private final List<Var> returnVars;

    private final Indexer<Var> varIndexer;

    private final List<Stmt> stmts;

    private final List<ExceptionEntry> exceptionEntries;

    public DefaultIR(
            JMethod method, Var thisVar,
            List<Var> params, Set<Var> returnVars, List<Var> vars,
            List<Stmt> stmts, List<ExceptionEntry> exceptionEntries) {
        this.method = method;
        this.thisVar = thisVar;
        this.params = List.copyOf(params);
        this.returnVars = List.copyOf(returnVars);
        this.vars = List.copyOf(vars);
        this.varIndexer = new VarIndexer();
        this.stmts = List.copyOf(stmts);
        this.exceptionEntries = List.copyOf(exceptionEntries);
    }

    @Override
    public JMethod getMethod() {
        return method;
    }

    @Override
    @Nullable
    public Var getThis() {
        return thisVar;
    }

    @Override
    public List<Var> getParams() {
        return params;
    }

    @Override
    public Var getParam(int i) {
        return params.get(i);
    }

    @Override
    public boolean isParam(Var var) {
        return params.contains(var);
    }

    @Override
    public boolean isThisOrParam(Var var) {
        return Objects.equals(thisVar, var) || isParam(var);
    }

    @Override
    public List<Var> getReturnVars() {
        return returnVars;
    }

    @Override
    public Var getVar(int i) {
        return vars.get(i);
    }

    @Override
    public List<Var> getVars() {
        return vars;
    }

    @Override
    public Indexer<Var> getVarIndexer() {
        return varIndexer;
    }

    private class VarIndexer implements Indexer<Var>, Serializable {

        @Override
        public int getIndex(Var v) {
            return v.getIndex();
        }

        @Override
        public Var getObject(int i) {
            return getVar(i);
        }
    }

    @Override
    public Stmt getStmt(int i) {
        return stmts.get(i);
    }

    @Override
    public List<Stmt> getStmts() {
        return stmts;
    }

    @Override
    public int getIndex(Stmt s) {
        return s.getIndex();
    }

    @Override
    public Stmt getObject(int i) {
        return getStmt(i);
    }

    @Override
    public List<ExceptionEntry> getExceptionEntries() {
        return exceptionEntries;
    }
}
