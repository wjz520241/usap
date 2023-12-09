

package keeno.usap.ir.proginfo;

import keeno.usap.language.classes.ClassMember;
import keeno.usap.language.classes.JClass;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Represents references to class members in IR.
 */
public abstract class MemberRef implements Serializable {

    private final JClass declaringClass;

    private final String name;

    private final boolean isStatic;

    public MemberRef(JClass declaringClass, String name, boolean isStatic) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.isStatic = isStatic;
    }

    /**
     * @return the declaring class of the reference.
     */
    public JClass getDeclaringClass() {
        return declaringClass;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return isStatic;
    }

    /**
     * @return the concrete class member pointed by this reference.
     * @throws ResolutionFailedException if the class member
     *                                   cannot be resolved.
     */
    public abstract ClassMember resolve();

    /**
     * @return the concrete class member pointed by this reference,
     * or null if the member cannot be resolved.
     */
    @Nullable
    public abstract ClassMember resolveNullable();
}
