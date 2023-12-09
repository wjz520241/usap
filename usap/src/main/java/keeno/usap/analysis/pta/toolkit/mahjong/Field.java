

package keeno.usap.analysis.pta.toolkit.mahjong;

import keeno.usap.language.classes.JField;
import keeno.usap.util.collection.Maps;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents edge labels of a field points-to graph, i.e., a JField or
 * a mock field that represents all array indexes.
 */
class Field {

    /**
     * When this is {@code null}, this Field represents array index.
     */
    @Nullable
    private final JField field;

    private Field(@Nullable JField field) {
        this.field = field;
    }

    @Override public String toString() {
        return field != null ? field.toString() : "@ARRAY-INDEX";
    }

    static class Factory {

        private static final Field ARRAY_INDEX = new Field(null);

        private final ConcurrentMap<JField, Field> fields = Maps.newConcurrentMap();

        Field get(JField field) {
            Objects.requireNonNull(field);
            return fields.computeIfAbsent(field, Field::new);
        }

        Field getArrayIndex() {
            return ARRAY_INDEX;
        }
    }
}
