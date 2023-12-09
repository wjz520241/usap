

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;

/**
 * Represents sources which generate taint objects on method parameters.
 *
 * @param method the method whose parameter are tainted. Usually, such methods
 *               are program entry points that receive inputs (treated as taints).
 * @param index  the index of the tainted parameter.
 * @param type   the type of the generated taint object.
 */
record ParamSource(JMethod method, int index, Type type) implements Source {

    @Override
    public String toString() {
        return String.format("ParamSource{%s/%s(%s)}",
                method, InvokeUtils.toString(index), type);
    }
}
