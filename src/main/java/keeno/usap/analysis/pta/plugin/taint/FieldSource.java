

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.language.classes.JField;
import keeno.usap.language.type.Type;

public record FieldSource(JField field, Type type) implements Source {

    @Override
    public String toString() {
        return String.format("FieldSource{%s(%s)}", field, type);
    }
}
