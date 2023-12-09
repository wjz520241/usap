

package keeno.usap.frontend.soot;

/**
 * Represents the errors raised during reading program information from soot.
 */
class SootFrontendException extends RuntimeException {

    SootFrontendException(String msg) {
        super(msg);
    }

    SootFrontendException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
