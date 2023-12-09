

package keeno.usap.analysis.pta.plugin.util;

import keeno.usap.World;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.MockObj;
import keeno.usap.ir.exp.ClassLiteral;
import keeno.usap.ir.exp.MethodHandle;
import keeno.usap.ir.exp.MethodType;
import keeno.usap.ir.exp.StringLiteral;
import keeno.usap.language.annotation.Annotation;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;

import javax.annotation.Nullable;

/**
 * Static utility methods for {@link CSObjs}.
 */
public final class CSObjs {

    private CSObjs() {
    }

    /**
     * @return {@code true} if {@code csObj} is {@link MockObj} and it has
     * descriptor {@code desc}.
     */
    public static boolean hasDescriptor(CSObj csObj, Descriptor desc) {
        return csObj.getObject() instanceof MockObj mockObj &&
                mockObj.getDescriptor().equals(desc);
    }

    /**
     * Converts a CSObj of string constant to corresponding String.
     * If the object is not a string constant, then return null.
     */
    @Nullable
    public static String toString(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return alloc instanceof StringLiteral str ? str.getString() : null;
    }

    /**
     * Converts a CSObj of class to corresponding JClass. If the object is
     * not a class constant, then return null.
     */
    @Nullable
    public static JClass toClass(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        if (alloc instanceof ClassLiteral cls) {
            Type type = cls.getTypeValue();
            if (type instanceof ClassType clsType) {
                return clsType.getJClass();
            } else if (type instanceof ArrayType) {
                return World.get().getClassHierarchy()
                        .getJREClass(ClassNames.OBJECT);
            }
        }
        return null;
    }

    /**
     * Converts a CSObj of java.lang.reflect.Constructor to corresponding JMethod.
     * If the object does not represent a Constructor, then return null.
     */
    @Nullable
    public static JMethod toConstructor(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return (alloc instanceof JMethod m && m.isConstructor()) ? m : null;
    }

    /**
     * Converts a CSObj of java.lang.reflect.Method to corresponding JMethod.
     * If the object does not represent a Method, then return null.
     */
    @Nullable
    public static JMethod toMethod(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return (alloc instanceof JMethod m && !m.isConstructor()) ? m : null;
    }

    /**
     * Converts a CSObj of java.lang.reflect.Method to corresponding JMethod.
     * If the object does not represent a Method, then return null.
     */
    @Nullable
    public static JField toField(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return alloc instanceof JField field ? field : null;
    }

    /**
     * Converts a CSObj of class to corresponding type. If the object is
     * not a class constant, then return null.
     */
    @Nullable
    public static Type toType(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return alloc instanceof ClassLiteral cls ? cls.getTypeValue() : null;
    }

    /**
     * Converts a CSObj of MethodType to corresponding MethodType.
     * If the object is not a MethodType, then return null.
     */
    @Nullable
    public static MethodType toMethodType(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return alloc instanceof MethodType mt ? mt : null;
    }

    /**
     * Converts a CSObj of MethodHandle constant to corresponding MethodHandle.
     * If the object is not a MethodHandle constant, then return null.
     */
    @Nullable
    public static MethodHandle toMethodHandle(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return alloc instanceof MethodHandle mh ? mh : null;
    }

    /**
     * Converts a CSObj of an Annotation to the Annotation.
     * If the object is not an Annotation object, then return null.
     */
    public static Annotation toAnnotation(CSObj csObj) {
        Object alloc = csObj.getObject().getAllocation();
        return alloc instanceof Annotation a ? a : null;
    }

    /**
     * @return {@code true} if given csObj is an array object.
     */
    public static boolean isArray(CSObj csObj) {
        return csObj.getObject().getType() instanceof ArrayType;
    }
}
