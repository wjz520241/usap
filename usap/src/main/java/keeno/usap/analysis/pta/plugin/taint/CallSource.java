

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;

/**
 * Represents sources which generates taint objects at method calls.
 *
 * @param method the method that generates taint object for variable at call site.
 * @param index  the index of the tainted variable at the call site.
 * @param type   type of the generated taint object.
 */
record CallSource(JMethod method, int index, Type type) implements Source {

    @Override
    public String toString() {
        return String.format("CallSource{%s/%s(%s)}",
                method, InvokeUtils.toString(index), type);
    }
}
