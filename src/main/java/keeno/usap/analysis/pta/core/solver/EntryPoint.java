

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.language.classes.JMethod;

/**
 * Represents entry points in pointer analysis. Each entry specifies:
 * <ol>
 *     <li>an entry method
 *     <li>the parameter objects provider for this variable/parameters of the entry method.
 * </ol>
 *
 * @see ParamProvider
 */
public record EntryPoint(JMethod method, ParamProvider paramProvider) {

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + method + '}';
    }
}
