

package keeno.usap.ir;

import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.JMethod;

import java.io.Serializable;

/**
 * Interface for builder of {@link IR}.
 */
public interface IRBuilder extends Serializable {

    /**
     * Builds IR for concrete methods.
     */
    IR buildIR(JMethod method);

    /**
     * Builds IR for all methods in the given hierarchy.
     */
    void buildAll(ClassHierarchy hierarchy);
}
