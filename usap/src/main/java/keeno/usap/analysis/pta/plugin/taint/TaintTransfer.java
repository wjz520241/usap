

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;

/**
 * Represents taint transfer between argument/base/return variables
 * caused by invocation to specific method.
 * <ul>
 *     <li>method: the method that causes taint transfer
 *     <li>from: the index of "from" variable
 *     <li>to: the index of "to" variable
 *     <li>type: the type of the transferred taint object
 * </ul>
 */
record TaintTransfer(
        JMethod method, TransferPoint from, TransferPoint to, Type type) {

    @Override
    public String toString() {
        return method + ": " +
                from + " -> " + to + "(" + type + ")";
    }
}
