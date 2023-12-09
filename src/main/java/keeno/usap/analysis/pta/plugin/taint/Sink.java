

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.language.classes.JMethod;

/**
 * Represents a sink in taint analysis.
 *
 * @param method the sink method.
 * @param index  the specific index used to locate the sensitive argument
 *               at the call site of {@code method}.
 */
record Sink(JMethod method, int index) {

    @Override
    public String toString() {
        return method + "/" + InvokeUtils.toString(index);
    }
}
