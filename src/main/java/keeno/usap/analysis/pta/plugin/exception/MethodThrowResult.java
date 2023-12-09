

package keeno.usap.analysis.pta.plugin.exception;

import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import java.util.Collections;
import java.util.Set;

import static keeno.usap.util.collection.Maps.newHybridMap;
import static keeno.usap.util.collection.Sets.newHybridSet;

public class MethodThrowResult {

    private final JMethod method;

    private final MultiMap<Stmt, Obj> explicitExceptions
            = Maps.newMultiMap(newHybridMap());

    private final Set<Obj> uncaughtExceptions = newHybridSet();

    public MethodThrowResult(JMethod method) {
        this.method = method;
    }

    public Set<Obj> mayThrowExplicitly(Stmt stmt) {
        return explicitExceptions.get(stmt);
    }

    public Set<Obj> mayThrowUncaught() {
        return Collections.unmodifiableSet(uncaughtExceptions);
    }

    void addCSMethodThrowResult(CSMethodThrowResult csMethodThrowResult) {
        for (Stmt stmt : method.getIR()) {
            csMethodThrowResult.mayThrowExplicitly(stmt)
                    .stream()
                    .map(CSObj::getObject)
                    .forEach(exception ->
                            explicitExceptions.put(stmt, exception));
        }
        csMethodThrowResult.mayThrowUncaught()
                .stream()
                .map(CSObj::getObject)
                .forEach(uncaughtExceptions::add);
    }

    @Override
    public String toString() {
        return "MethodThrowResult{" +
                "method=" + method +
                ", explicitExceptions=" + explicitExceptions +
                ", uncaughtExceptions=" + uncaughtExceptions +
                '}';
    }
}
