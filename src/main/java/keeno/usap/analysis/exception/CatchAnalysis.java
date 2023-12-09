

package keeno.usap.analysis.exception;

import keeno.usap.World;
import keeno.usap.ir.IR;
import keeno.usap.ir.proginfo.ExceptionEntry;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.Throw;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Intra-procedural catch analysis for computing the exceptions thrown by
 * each Stmt will be caught by which Stmts, or not caught at all.
 */
public class CatchAnalysis {

    /**
     * Analyzes the exceptions thrown by each Stmt in given IR may be caught
     * by which (catch) Stmts, and which exceptions are not caught in the IR.
     */
    public static CatchResult analyze(IR ir, ThrowResult throwResult) {
        Map<Stmt, List<ExceptionEntry>> catchers = getPotentialCatchers(ir);
        TypeSystem typeSystem = World.get().getTypeSystem();
        CatchResult result = new CatchResult();
        ir.forEach(stmt -> {
            Collection<ClassType> implicit = throwResult.mayThrowImplicitly(stmt);
            Collection<ClassType> explicit;
            if (stmt instanceof Throw) {
                explicit = throwResult.mayThrowExplicitly((Throw) stmt);
            } else if (stmt instanceof Invoke) {
                explicit = throwResult.mayThrowExplicitly((Invoke) stmt);
            } else {
                explicit = List.of();
            }
            for (ExceptionEntry entry : catchers.getOrDefault(stmt, List.of())) {
                Set<ClassType> uncaughtImplicit = Sets.newHybridSet();
                implicit.forEach(t -> {
                    if (typeSystem.isSubtype(entry.catchType(), t)) {
                        result.addCaughtImplicit(stmt, entry.handler(), t);
                    } else {
                        uncaughtImplicit.add(t);
                    }
                });
                implicit = uncaughtImplicit;

                Set<ClassType> uncaughtExplicit = Sets.newHybridSet();
                explicit.forEach(t -> {
                    if (typeSystem.isSubtype(entry.catchType(), t)) {
                        result.addCaughtExplicit(stmt, entry.handler(), t);
                    } else {
                        uncaughtExplicit.add(t);
                    }
                });
                explicit = uncaughtExplicit;
            }
            implicit.forEach(e -> result.addUncaughtImplicit(stmt, e));
            explicit.forEach(e -> result.addUncaughtExplicit(stmt, e));
        });
        return result;
    }

    /**
     * @return a map from each Stmt to a list of exception entries which
     * may catch the exceptions thrown by the Stmt.
     */
    public static Map<Stmt, List<ExceptionEntry>> getPotentialCatchers(IR ir) {
        Map<Stmt, List<ExceptionEntry>> catchers = Maps.newLinkedHashMap();
        ir.getExceptionEntries().forEach(entry -> {
            for (int i = entry.start().getIndex(); i < entry.end().getIndex(); ++i) {
                Stmt stmt = ir.getStmt(i);
                catchers.computeIfAbsent(stmt, __ -> new ArrayList<>())
                        .add(entry);
            }
        });
        return catchers;
    }
}
