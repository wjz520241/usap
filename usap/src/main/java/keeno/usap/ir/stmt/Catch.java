

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.LValue;
import keeno.usap.ir.exp.Var;

import java.util.Optional;

/**
 * Representation of catch exception, e.g., catch (e).
 */
public class Catch extends AbstractStmt {

    /**
     * Reference of the exception object to be caught.
     */
    private final Var exceptionRef;

    public Catch(Var exceptionRef) {
        this.exceptionRef = exceptionRef;
    }

    public Var getExceptionRef() {
        return exceptionRef;
    }

    @Override
    public Optional<LValue> getDef() {
        return Optional.of(exceptionRef);
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "catch " + exceptionRef;
    }
}
