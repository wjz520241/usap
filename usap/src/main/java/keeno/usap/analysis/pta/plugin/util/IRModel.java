

package keeno.usap.analysis.pta.plugin.util;

import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.language.classes.JMethod;

import java.util.Set;

/**
 * Models specific APIs by generating corresponding IR.
 */
public interface IRModel {

    Set<JMethod> getModeledAPIs();

    void handleNewMethod(JMethod method);

    void handleNewCSMethod(CSMethod csMethod);
}
