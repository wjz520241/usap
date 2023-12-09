

package keeno.usap.analysis.exception;

import keeno.usap.World;
import keeno.usap.analysis.pta.PointerAnalysis;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.plugin.exception.ExceptionAnalysis;
import keeno.usap.analysis.pta.plugin.exception.PTAThrowResult;
import keeno.usap.ir.IR;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Throw;
import keeno.usap.language.type.ClassType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Analyzes explicit exceptions based on pointer analysis.
 */
class PTABasedExplicitThrowAnalysis implements ExplicitThrowAnalysis {

    private final PTAThrowResult ptaThrowResult;

    PTABasedExplicitThrowAnalysis() {
        PointerAnalysisResult result = World.get().getResult(PointerAnalysis.ID);
        this.ptaThrowResult = result.getResult(ExceptionAnalysis.class.getName());
    }

    @Override
    public void analyze(IR ir, ThrowResult result) {
        ptaThrowResult.getResult(ir.getMethod())
                .ifPresent(ptaResult ->
                        ir.forEach(stmt -> {
                            Set<ClassType> exceptions = ptaResult
                                    .mayThrowExplicitly(stmt)
                                    .stream()
                                    .map(o -> (ClassType) o.getType())
                                    .collect(Collectors.toUnmodifiableSet());
                            if (!exceptions.isEmpty()) {
                                if (stmt instanceof Throw) {
                                    result.addExplicit((Throw) stmt, exceptions);
                                } else if (stmt instanceof Invoke) {
                                    result.addExplicit((Invoke) stmt, exceptions);
                                }
                            }
                        })
                );
    }
}
