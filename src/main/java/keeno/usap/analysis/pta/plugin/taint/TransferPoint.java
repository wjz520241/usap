

package keeno.usap.analysis.pta.plugin.taint;

import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.language.classes.JField;

record TransferPoint(Kind kind, int index, JField field) {

    static final String ARRAY_SUFFIX = "[*]";

    enum Kind {
        VAR, ARRAY, FIELD
    }

    @Override
    public JField field() {
        assert kind == Kind.FIELD : "Not a FIELD TransferPoint";
        return field;
    }

    @Override
    public String toString() {
        String base = InvokeUtils.toString(index);
        return switch (kind) {
            case VAR -> base;
            case ARRAY -> base + ARRAY_SUFFIX;
            case FIELD -> base + "." + field.getName();
        };
    }
}
