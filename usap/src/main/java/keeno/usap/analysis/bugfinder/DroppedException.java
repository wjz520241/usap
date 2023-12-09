

package keeno.usap.analysis.bugfinder;

import keeno.usap.analysis.MethodAnalysis;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.IR;
import keeno.usap.ir.proginfo.ExceptionEntry;
import keeno.usap.ir.stmt.Goto;
import keeno.usap.ir.stmt.Nop;
import keeno.usap.ir.stmt.Return;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.util.collection.Sets;

import java.util.Set;

public class DroppedException extends MethodAnalysis<Set<BugInstance>> {

    public static final String ID = "dropped-exception";

    public DroppedException(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Set<BugInstance> analyze(IR ir) {
        Set<BugInstance> bugInstanceSet = Sets.newHybridSet();
        for (ExceptionEntry entry : ir.getExceptionEntries()) {
            String exceptionName = entry.catchType().getName();
            if (exceptionName.equals(ClassNames.CLONE_NOT_SUPPORTED_EXCEPTION)
                    || exceptionName.equals(ClassNames.INTERRUPTED_EXCEPTION)) {
                continue;
            }
            Stmt catchHandler = entry.handler();
            int nextStmt = catchHandler.getIndex() + 1;

            while (nextStmt < ir.getStmts().size() && ir.getStmt(nextStmt) instanceof Nop) {
                nextStmt++;
            }

            if (nextStmt < ir.getStmts().size() &&
                    (ir.getStmt(nextStmt) instanceof Goto || ir.getStmt(nextStmt) instanceof Return)) {
                boolean exitInTryBlock = false;
                for (int i = entry.start().getIndex(); i <= entry.end().getIndex(); ++i) {
                    if (ir.getStmt(i) instanceof Return) {
                        exitInTryBlock = true;
                        break;
                    }
                }
                Severity severity = Severity.MINOR;
                if (exceptionName.equals(ClassNames.ERROR)
                        || exceptionName.equals(ClassNames.EXCEPTION)
                        || exceptionName.equals(ClassNames.THROWABLE)
                        || exceptionName.equals(ClassNames.RUNTIME_EXCEPTION)) {
                    severity = Severity.CRITICAL;
                }
                BugInstance bugInstance = new BugInstance(
                        exitInTryBlock ? BugType.DE_MIGHT_DROP : BugType.DE_MIGHT_IGNORE,
                        severity, ir.getMethod())
                        .setSourceLine(catchHandler.getLineNumber());
                bugInstanceSet.add(bugInstance);
            }
        }

        return bugInstanceSet;
    }

    private enum BugType implements keeno.usap.analysis.bugfinder.BugType {
        DE_MIGHT_DROP,
        DE_MIGHT_IGNORE
    }
}
