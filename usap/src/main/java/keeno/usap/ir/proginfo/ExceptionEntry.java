

package keeno.usap.ir.proginfo;

import keeno.usap.ir.stmt.Catch;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.type.ClassType;

import java.io.Serializable;

/**
 * Representation of exception entries. Each entry consists of four items:
 * <ul>
 *     <li>start: the beginning of the try-block (inclusive).
 *     <li>end: the end of the try-block (exclusive).
 *     <li>handler: the beginning of the catch-block (inclusive),
 *     i.e., the handler for the exceptions thrown by the try-block.
 *     <li>catchType: the class of exceptions that this exception handler
 *     is designated to catch.
 * </ul>
 */
public record ExceptionEntry(Stmt start, Stmt end,
                             Catch handler, ClassType catchType)
        implements Serializable {

    @Override
    public String toString() {
        return String.format("try [%d, %d), catch %s at %d",
                start.getIndex(), end.getIndex(),
                catchType, handler.getIndex());
    }
}
