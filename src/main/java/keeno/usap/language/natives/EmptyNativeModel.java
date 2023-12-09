

package keeno.usap.language.natives;

import keeno.usap.ir.IR;
import keeno.usap.ir.IRBuildHelper;
import keeno.usap.language.classes.JMethod;

/**
 * Builds empty IR for every native method.
 */
public class EmptyNativeModel implements NativeModel {

    @Override
    public IR buildNativeIR(JMethod method) {
        return new IRBuildHelper(method).buildEmpty();
    }
}
