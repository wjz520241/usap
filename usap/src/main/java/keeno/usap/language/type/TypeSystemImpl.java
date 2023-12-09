

package keeno.usap.language.type;

import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JClassLoader;
import keeno.usap.util.AnalysisException;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static keeno.usap.util.collection.Maps.newConcurrentMap;
import static keeno.usap.util.collection.Maps.newMap;
import static keeno.usap.util.collection.Maps.newSmallMap;

// TODO: optimize maps (classTypes and arrayTypes)
public class TypeSystemImpl implements TypeSystem {

    private final ClassHierarchy hierarchy;

    private final Map<JClassLoader, Map<String, ClassType>> classTypes = newSmallMap();

    /**
     * This map may be concurrently written during IR construction,
     * thus we use concurrent map to ensure its thread-safety.
     */
    private final ConcurrentMap<Integer, ConcurrentMap<Type, ArrayType>> arrayTypes
            = newConcurrentMap(8);

    private final ClassType OBJECT;
    private final ClassType SERIALIZABLE;
    private final ClassType CLONEABLE;

    // Boxed types
    private final ClassType BOOLEAN;
    private final ClassType BYTE;
    private final ClassType SHORT;
    private final ClassType CHARACTER;
    private final ClassType INTEGER;
    private final ClassType LONG;
    private final ClassType FLOAT;
    private final ClassType DOUBLE;

    public TypeSystemImpl(ClassHierarchy hierarchy) {
        this.hierarchy = hierarchy;
        // Initialize special types
        JClassLoader loader = hierarchy.getBootstrapClassLoader();
        OBJECT = getClassType(loader, ClassNames.OBJECT);
        SERIALIZABLE = getClassType(loader, ClassNames.SERIALIZABLE);
        CLONEABLE = getClassType(loader, ClassNames.CLONEABLE);
        BOOLEAN = getClassType(loader, ClassNames.BOOLEAN);
        BYTE = getClassType(loader, ClassNames.BYTE);
        SHORT = getClassType(loader, ClassNames.SHORT);
        CHARACTER = getClassType(loader, ClassNames.CHARACTER);
        INTEGER = getClassType(loader, ClassNames.INTEGER);
        LONG = getClassType(loader, ClassNames.LONG);
        FLOAT = getClassType(loader, ClassNames.FLOAT);
        DOUBLE = getClassType(loader, ClassNames.DOUBLE);
    }

    @Override
    public Type getType(JClassLoader loader, String typeName) {
        try {
            if (typeName.endsWith("[]")) {
                int dim = 0;
                int i = typeName.length() - 1;
                while (i > 0) {
                    if (typeName.charAt(i - 1) == '[' && typeName.charAt(i) == ']') {
                        ++dim;
                        i -= 2;
                    } else {
                        break;
                    }
                }
                return getArrayType(
                        getType(loader, typeName.substring(0, i + 1)),
                        dim);
            } else if (PrimitiveType.isPrimitiveType(typeName)) {
                return PrimitiveType.get(typeName);
            } else {
                return getClassType(loader, typeName);
            }
        } catch (Exception e) {
            throw new AnalysisException("Invalid type name: " + typeName, e);
        }
    }

    @Override
    public Type getType(String typeName) {
        return getType(hierarchy.getDefaultClassLoader(), typeName);
    }

    @Override
    public ClassType getClassType(JClassLoader loader, String className) {
        // FIXME: given a non-exist class name, this method will still return
        //  a ClassType with null JClass. This case should return null.
        return classTypes.computeIfAbsent(loader, l -> newMap())
                .computeIfAbsent(className, name -> new ClassType(loader, name));
    }

    @Override
    public ClassType getClassType(String className) {
        // TODO: add warning for missing class loader
        return getClassType(hierarchy.getDefaultClassLoader(), className);
    }

    @Override
    public ArrayType getArrayType(Type baseType, int dim) {
        assert !(baseType instanceof VoidType)
                && !(baseType instanceof NullType);
        assert dim >= 1;
        return arrayTypes.computeIfAbsent(dim, d -> newConcurrentMap())
                .computeIfAbsent(baseType, t ->
                        new ArrayType(t, dim,
                                dim == 1 ? t : getArrayType(t, dim - 1)));
    }

    @Override
    public ClassType getBoxedType(PrimitiveType type) {
        return switch (type) {
            case BOOLEAN -> BOOLEAN;
            case BYTE -> BYTE;
            case SHORT -> SHORT;
            case CHAR -> CHARACTER;
            case INT -> INTEGER;
            case LONG -> LONG;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
        };
    }

    @Override
    public PrimitiveType getUnboxedType(ClassType type) {
        if (type.equals(BOOLEAN)) {
            return PrimitiveType.BOOLEAN;
        } else if (type.equals(BYTE)) {
            return PrimitiveType.BYTE;
        } else if (type.equals(SHORT)) {
            return PrimitiveType.SHORT;
        } else if (type.equals(CHARACTER)) {
            return PrimitiveType.CHAR;
        } else if (type.equals(INTEGER)) {
            return PrimitiveType.INT;
        } else if (type.equals(LONG)) {
            return PrimitiveType.LONG;
        } else if (type.equals(FLOAT)) {
            return PrimitiveType.FLOAT;
        } else if (type.equals(DOUBLE)) {
            return PrimitiveType.DOUBLE;
        }
        throw new AnalysisException(type + " cannot be unboxed");
    }

    @Override
    public boolean isSubtype(Type supertype, Type subtype) {
        if (subtype.equals(supertype)) {
            return true;
        } else if (subtype instanceof NullType) {
            return supertype instanceof ReferenceType;
        } else if (subtype instanceof ClassType) {
            if (supertype instanceof ClassType) {
                return hierarchy.isSubclass(
                        ((ClassType) supertype).getJClass(),
                        ((ClassType) subtype).getJClass());
            }
        } else if (subtype instanceof ArrayType) {
            if (supertype instanceof ClassType) {
                // JLS (11 Ed.), Chapter 10, Arrays
                return supertype == OBJECT ||
                        supertype == CLONEABLE ||
                        supertype == SERIALIZABLE;
            } else if (supertype instanceof ArrayType superArray) {
                ArrayType subArray = (ArrayType) subtype;
                Type superBase = superArray.baseType();
                Type subBase = subArray.baseType();
                if (superArray.dimensions() == subArray.dimensions()) {
                    if (subBase.equals(superBase)) {
                        return true;
                    } else if (superBase instanceof ClassType &&
                            subBase instanceof ClassType) {
                        return hierarchy.isSubclass(
                                ((ClassType) superBase).getJClass(),
                                ((ClassType) subBase).getJClass());
                    }
                } else if (superArray.dimensions() < subArray.dimensions()) {
                    return superBase == OBJECT ||
                            superBase == CLONEABLE ||
                            superBase == SERIALIZABLE;
                }
            }
        }
        return false;
    }
}
