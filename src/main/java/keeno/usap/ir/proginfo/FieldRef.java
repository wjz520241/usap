

package keeno.usap.ir.proginfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.StringReps;
import keeno.usap.language.type.Type;
import keeno.usap.util.InternalCanonicalized;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Sets;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents field references in IR.
 */
@InternalCanonicalized
public class FieldRef extends MemberRef {

    private static final Logger logger = LogManager.getLogger(FieldRef.class);

    private static final ConcurrentMap<Key, FieldRef> map =
            Maps.newConcurrentMap(4096);

    /**
     * Records the FieldRef that fails to be resolved.
     */
    private static final Set<FieldRef> resolveFailures =
            Sets.newConcurrentSet();

    static {
        World.registerResetCallback(map::clear);
        World.registerResetCallback(resolveFailures::clear);
    }

    private final Type type;

    /**
     * Caches the resolved field for this reference to avoid redundant
     * field resolution.
     *
     * @see #resolve()
     * @see #resolveNullable()
     */
    @Nullable
    private transient JField field;

    public static FieldRef get(
            JClass declaringClass, String name, Type type, boolean isStatic) {
        Key key = new Key(declaringClass, name, type);
        return map.computeIfAbsent(key, k -> new FieldRef(k, isStatic));
    }

    private FieldRef(Key key, boolean isStatic) {
        super(key.declaringClass, key.name, isStatic);
        this.type = key.type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public JField resolve() {
        if (field == null) {
            field = World.get().getClassHierarchy()
                    .resolveField(this);
            if (field == null) {
                throw new FieldResolutionFailedException(
                        "Cannot resolve " + this);
            }
        }
        return field;
    }

    @Override
    @Nullable
    public JField resolveNullable() {
        if (field == null) {
            field = World.get().getClassHierarchy()
                    .resolveField(this);
            if (field == null && resolveFailures.add(this)) {
                logger.warn("Failed to resolve {}", this);
            }
        }
        return field;
    }

    @Override
    public String toString() {
        return StringReps.getFieldSignature(
                getDeclaringClass(), getName(), type);
    }

    /**
     * Uses as keys to identify {@link FieldRef}s in cache.
     */
    private record Key(JClass declaringClass, String name, Type type) {
    }
}
