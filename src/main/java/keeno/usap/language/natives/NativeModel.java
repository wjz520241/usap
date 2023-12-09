

package keeno.usap.language.natives;

import keeno.usap.ir.IR;
import keeno.usap.language.classes.JMethod;

import java.io.Serializable;

public interface NativeModel extends Serializable {

    IR buildNativeIR(JMethod method);
}
