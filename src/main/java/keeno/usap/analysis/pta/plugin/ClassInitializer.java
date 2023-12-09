

package keeno.usap.analysis.pta.plugin;

import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.ir.stmt.AssignLiteral;
import keeno.usap.ir.stmt.FieldStmt;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;

/**
 * Triggers the analysis of class initializers.
 * Well, the description of "when initialization occurs" of JLS (11 Ed., 12.4.1)
 * and JVM Spec. (11 Ed., 5.5) looks not very consistent.
 * TODO: handles class initialization triggered by MethodHandle,
 *  and superinterfaces (that declare default methods).
 */
public class ClassInitializer implements Plugin {

    private Solver solver;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void onNewMethod(JMethod method) {
        if (method.isStatic() || method.isConstructor()) {
            solver.initializeClass(method.getDeclaringClass());
        }
    }

    @Override
    public void onNewStmt(Stmt stmt, JMethod container) {
        if (stmt instanceof AssignLiteral) {
            Type type = ((AssignLiteral) stmt).getRValue().getType();
            if (type instanceof ClassType) {
                solver.initializeClass(((ClassType) type).getJClass());
            }
        } else if (stmt instanceof FieldStmt<?, ?> fieldStmt) {
            if (fieldStmt.isStatic()) {
                JField field = fieldStmt.getFieldRef().resolve();
                solver.initializeClass(field.getDeclaringClass());
            }
        }
    }
}
