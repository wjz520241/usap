

package keeno.usap.util.collection;

/**
 * Thrown by fixed-capacity collections to indicate that the number of
 * elements added to the collection exceeds its fixed capacity.
 */
public class TooManyElementsException extends RuntimeException {

    /**
     * Constructs a new exception.
     */
    public TooManyElementsException() {
    }

    /**
     * Constructs a new exception.
     */
    public TooManyElementsException(String msg) {
        super(msg);
    }
}
