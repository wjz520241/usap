

package keeno.usap.language.classes;

/**
 * Exception that is thrown when a member (method or field) is accessed
 * through an ambiguous name.
 */
public class AmbiguousMemberException extends RuntimeException {

    public AmbiguousMemberException(String className, String memberName) {
        super(String.format("%s has multiple members with name %s", className, memberName));
    }
}
