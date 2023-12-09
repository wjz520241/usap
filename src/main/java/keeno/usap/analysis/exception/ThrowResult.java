

package keeno.usap.analysis.exception;

import keeno.usap.ir.IR;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.Throw;
import keeno.usap.language.type.ClassType;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import java.util.Collection;
import java.util.Set;

public class ThrowResult {

    private final IR ir;

    /**
     * If this field is null, then this result returns empty collection
     * for implicit exceptions.
     */
    private final ImplicitThrowAnalysis implicit;

    private final MultiMap<Stmt, ClassType> explicitExceptions = Maps.newMultiMap();

    ThrowResult(IR ir, ImplicitThrowAnalysis implicitThrowAnalysis) {
        this.ir = ir;
        this.implicit = implicitThrowAnalysis;
    }

    void addExplicit(Throw throwStmt, Collection<ClassType> exceptions) {
        explicitExceptions.putAll(throwStmt, exceptions);
    }

    void addExplicit(Invoke invoke, Collection<ClassType> exceptions) {
        explicitExceptions.putAll(invoke, exceptions);
    }

    public IR getIR() {
        return ir;
    }

    public Set<ClassType> mayThrowImplicitly(Stmt stmt) {
        return implicit == null ? Set.of() :
                implicit.mayThrowImplicitly(stmt);
    }

    public Set<ClassType> mayThrowExplicitly(Throw throwStmt) {
        return explicitExceptions.get(throwStmt);
    }

    public Set<ClassType> mayThrowExplicitly(Invoke invoke) {
        return explicitExceptions.get(invoke);
    }
}
