

package keeno.usap.language.type;

import keeno.usap.language.classes.JClassLoader;

import java.io.Serializable;

/**
 * This class provides APIs for retrieving types in the analyzed program.
 * For convenience, the special predefined types, i.e., primitive types,
 * null type, and void type can be directly retrieved from their own classes.
 */
public interface TypeSystem extends Serializable {

    Type getType(JClassLoader loader, String typeName);

    Type getType(String typeName);

    ClassType getClassType(JClassLoader loader, String className);

    ClassType getClassType(String className);

    ArrayType getArrayType(Type baseType, int dimensions);

    ClassType getBoxedType(PrimitiveType type);

    PrimitiveType getUnboxedType(ClassType type);

    boolean isSubtype(Type supertype, Type subtype);
}
