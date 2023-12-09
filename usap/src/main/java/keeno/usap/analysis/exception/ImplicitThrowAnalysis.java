

package keeno.usap.analysis.exception;

import keeno.usap.World;
import keeno.usap.ir.exp.ArithmeticExp;
import keeno.usap.ir.exp.ArrayLengthExp;
import keeno.usap.ir.exp.NewInstance;
import keeno.usap.ir.stmt.Binary;
import keeno.usap.ir.stmt.Cast;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.LoadArray;
import keeno.usap.ir.stmt.LoadField;
import keeno.usap.ir.stmt.Monitor;
import keeno.usap.ir.stmt.New;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.StmtVisitor;
import keeno.usap.ir.stmt.StoreArray;
import keeno.usap.ir.stmt.StoreField;
import keeno.usap.ir.stmt.Throw;
import keeno.usap.ir.stmt.Unary;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.TypeSystem;

import java.util.Set;

class ImplicitThrowAnalysis {

    // Implicit exception groups
    private final Set<ClassType> ARITHMETIC_EXCEPTION;

    private final Set<ClassType> LOAD_ARRAY_EXCEPTIONS;

    private final Set<ClassType> STORE_ARRAY_EXCEPTIONS;

    private final Set<ClassType> INITIALIZER_ERROR;

    private final Set<ClassType> CLASS_CAST_EXCEPTION;

    private final Set<ClassType> NEW_ARRAY_EXCEPTIONS;

    private final Set<ClassType> NULL_POINTER_EXCEPTION;

    private final Set<ClassType> OUT_OF_MEMORY_ERROR;

    /**
     * Visitor for compute implicit exceptions that may be thrown by each Stmt.
     */
    private final StmtVisitor<Set<ClassType>> implicitVisitor
            = new StmtVisitor<>() {
        @Override
        public Set<ClassType> visit(New stmt) {
            return stmt.getRValue() instanceof NewInstance ?
                    OUT_OF_MEMORY_ERROR : NEW_ARRAY_EXCEPTIONS;
        }

        @Override
        public Set<ClassType> visit(LoadArray stmt) {
            return LOAD_ARRAY_EXCEPTIONS;
        }

        @Override
        public Set<ClassType> visit(StoreArray stmt) {
            return STORE_ARRAY_EXCEPTIONS;
        }

        @Override
        public Set<ClassType> visit(LoadField stmt) {
            return stmt.isStatic() ?
                    INITIALIZER_ERROR : NULL_POINTER_EXCEPTION;
        }

        @Override
        public Set<ClassType> visit(StoreField stmt) {
            return stmt.isStatic() ?
                    INITIALIZER_ERROR : NULL_POINTER_EXCEPTION;
        }

        @Override
        public Set<ClassType> visit(Binary stmt) {
            if (stmt.getRValue() instanceof ArithmeticExp) {
                ArithmeticExp.Op op = ((ArithmeticExp) stmt.getRValue())
                        .getOperator();
                if (op == ArithmeticExp.Op.DIV || op == ArithmeticExp.Op.REM) {
                    return ARITHMETIC_EXCEPTION;
                }
            }
            return Set.of();
        }

        @Override
        public Set<ClassType> visit(Unary stmt) {
            return stmt.getRValue() instanceof ArrayLengthExp ?
                    NULL_POINTER_EXCEPTION : Set.of();
        }

        @Override
        public Set<ClassType> visit(Cast stmt) {
            return CLASS_CAST_EXCEPTION;
        }

        @Override
        public Set<ClassType> visit(Invoke stmt) {
            return stmt.isStatic() ?
                    INITIALIZER_ERROR : NULL_POINTER_EXCEPTION;
        }

        @Override
        public Set<ClassType> visit(Throw stmt) {
            return NULL_POINTER_EXCEPTION;
        }

        @Override
        public Set<ClassType> visit(Monitor stmt) {
            return NULL_POINTER_EXCEPTION;
        }

        @Override
        public Set<ClassType> visitDefault(Stmt stmt) {
            return Set.of();
        }
    };

    ImplicitThrowAnalysis() {
        TypeSystem ts = World.get().getTypeSystem();
        ClassType arrayStoreException = ts.getClassType(ClassNames.ARRAY_STORE_EXCEPTION);
        ClassType indexOutOfBoundsException = ts.getClassType(ClassNames.INDEX_OUT_OF_BOUNDS_EXCEPTION);
        ClassType nullPointerException = ts.getClassType(ClassNames.NULL_POINTER_EXCEPTION);
        ClassType outOfMemoryError = ts.getClassType(ClassNames.OUT_OF_MEMORY_ERROR);

        ARITHMETIC_EXCEPTION = Set.of(
                ts.getClassType(ClassNames.ARITHMETIC_EXCEPTION));
        LOAD_ARRAY_EXCEPTIONS = Set.of(
                indexOutOfBoundsException,
                nullPointerException);
        STORE_ARRAY_EXCEPTIONS = Set.of(
                arrayStoreException,
                indexOutOfBoundsException,
                nullPointerException);
        INITIALIZER_ERROR = Set.of(
                ts.getClassType(ClassNames.EXCEPTION_IN_INITIALIZER_ERROR));
        CLASS_CAST_EXCEPTION = Set.of(
                ts.getClassType(ClassNames.CLASS_CAST_EXCEPTION));
        NEW_ARRAY_EXCEPTIONS = Set.of(
                outOfMemoryError,
                ts.getClassType(ClassNames.NEGATIVE_ARRAY_SIZE_EXCEPTION));
        NULL_POINTER_EXCEPTION = Set.of(nullPointerException);
        OUT_OF_MEMORY_ERROR = Set.of(outOfMemoryError);
    }

    Set<ClassType> mayThrowImplicitly(Stmt stmt) {
        return stmt.accept(implicitVisitor);
    }

}
