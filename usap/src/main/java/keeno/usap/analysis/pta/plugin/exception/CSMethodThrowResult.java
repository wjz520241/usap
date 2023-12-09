

package keeno.usap.analysis.pta.plugin.exception;

import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.SetEx;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CSMethodThrowResult {

    private final Supplier<SetEx<CSObj>> setFactory;

    private final Map<Stmt, SetEx<CSObj>> explicitExceptions;

    private final SetEx<CSObj> uncaughtExceptions;

    CSMethodThrowResult(Supplier<SetEx<CSObj>> setFactory) {
        this.setFactory = setFactory;
        explicitExceptions = Maps.newHybridMap();
        uncaughtExceptions = setFactory.get();
    }

    Set<CSObj> propagate(Stmt stmt, Set<CSObj> exceptions) {
        return explicitExceptions.computeIfAbsent(stmt, __ -> setFactory.get())
                .addAllDiff(exceptions);
    }

    void addUncaughtExceptions(Set<CSObj> exceptions) {
        uncaughtExceptions.addAll(exceptions);
    }

    Set<CSObj> mayThrowExplicitly(Stmt stmt) {
        Set<CSObj> result = explicitExceptions.get(stmt);
        return result != null ? result : Set.of();
    }

    Set<CSObj> mayThrowUncaught() {
        return Collections.unmodifiableSet(uncaughtExceptions);
    }
}
