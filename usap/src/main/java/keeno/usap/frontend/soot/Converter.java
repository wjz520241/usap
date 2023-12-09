

package keeno.usap.frontend.soot;

import keeno.usap.ir.proginfo.FieldRef;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.language.annotation.Annotation;
import keeno.usap.language.annotation.AnnotationElement;
import keeno.usap.language.annotation.AnnotationHolder;
import keeno.usap.language.annotation.ArrayElement;
import keeno.usap.language.annotation.BooleanElement;
import keeno.usap.language.annotation.ClassElement;
import keeno.usap.language.annotation.DoubleElement;
import keeno.usap.language.annotation.Element;
import keeno.usap.language.annotation.EnumElement;
import keeno.usap.language.annotation.FloatElement;
import keeno.usap.language.annotation.IntElement;
import keeno.usap.language.annotation.LongElement;
import keeno.usap.language.annotation.StringElement;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JClassLoader;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.classes.StringReps;
import keeno.usap.language.generics.GSignatures;
import keeno.usap.language.generics.MethodGSignature;
import keeno.usap.language.generics.ReferenceTypeGSignature;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.Type;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.util.collection.Lists;
import keeno.usap.util.collection.Maps;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.PrimType;
import soot.RefType;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.VoidType;
import soot.jimple.toolkits.typing.fast.BottomType;
import soot.tagkit.AbstractHost;
import soot.tagkit.AnnotationAnnotationElem;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationBooleanElem;
import soot.tagkit.AnnotationClassElem;
import soot.tagkit.AnnotationDoubleElem;
import soot.tagkit.AnnotationElem;
import soot.tagkit.AnnotationEnumElem;
import soot.tagkit.AnnotationFloatElem;
import soot.tagkit.AnnotationIntElem;
import soot.tagkit.AnnotationLongElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.ParamNamesTag;
import soot.tagkit.SignatureTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static keeno.usap.language.type.BottomType.BOTTOM;
import static keeno.usap.language.type.VoidType.VOID;
import static keeno.usap.util.collection.Maps.newConcurrentMap;

/**
 * Converts Soot classes to Tai-e's representation.
 */
class Converter {

    private final JClassLoader loader;

    private final TypeSystem typeSystem;

    // Following four maps may be concurrently written during IR construction,
    // thus we use concurrent map to ensure their thread-safety.
    private final ConcurrentMap<SootField, JField> fieldMap
            = newConcurrentMap(4096);

    private final ConcurrentMap<SootMethod, JMethod> methodMap
            = newConcurrentMap(4096);

    private final ConcurrentMap<SootFieldRef, FieldRef> fieldRefMap
            = newConcurrentMap(4096);

    private final ConcurrentMap<SootMethodRef, MethodRef> methodRefMap
            = newConcurrentMap(4096);

    Converter(JClassLoader loader, TypeSystem typeSystem) {
        this.loader = loader;
        this.typeSystem = typeSystem;
    }

    Type convertType(soot.Type sootType) {
        if (sootType instanceof PrimType) {
            if (sootType instanceof ByteType) {
                return PrimitiveType.BYTE;
            } else if (sootType instanceof ShortType) {
                return PrimitiveType.SHORT;
            } else if (sootType instanceof IntType) {
                return PrimitiveType.INT;
            } else if (sootType instanceof LongType) {
                return PrimitiveType.LONG;
            } else if (sootType instanceof FloatType) {
                return PrimitiveType.FLOAT;
            } else if (sootType instanceof DoubleType) {
                return PrimitiveType.DOUBLE;
            } else if (sootType instanceof CharType) {
                return PrimitiveType.CHAR;
            } else if (sootType instanceof BooleanType) {
                return PrimitiveType.BOOLEAN;
            }
        } else if (sootType instanceof RefType) {
            return typeSystem.getClassType(loader, sootType.toString());
        } else if (sootType instanceof ArrayType arrayType) {
            return typeSystem.getArrayType(
                    convertType(arrayType.baseType),
                    arrayType.numDimensions);
        } else if (sootType instanceof VoidType) {
            return VOID;
        } else if (sootType instanceof BottomType) {
            return BOTTOM;
        }
        throw new SootFrontendException("Cannot convert soot Type: " + sootType);
    }

    JClass convertClass(SootClass sootClass) {
        return loader.loadClass(sootClass.getName());
    }

    JField convertField(SootField sootField) {
        return fieldMap.computeIfAbsent(sootField, f ->
                new JField(convertClass(sootField.getDeclaringClass()),
                        sootField.getName(),
                        Modifiers.convert(sootField.getModifiers()),
                        convertType(sootField.getType()),
                        convertGSignature(sootField),
                        convertAnnotations(sootField)));
    }

    JMethod convertMethod(SootMethod sootMethod) {
        return methodMap.computeIfAbsent(sootMethod, m -> {
            List<Type> paramTypes = Lists.map(
                    m.getParameterTypes(), this::convertType);
            Type returnType = convertType(m.getReturnType());
            List<ClassType> exceptions = Lists.map(
                    m.getExceptions(),
                    sc -> (ClassType) convertType(sc.getType()));
            // TODO: convert attributes
            return new JMethod(convertClass(m.getDeclaringClass()),
                    m.getName(), Modifiers.convert(m.getModifiers()),
                    paramTypes, returnType, exceptions,
                    convertGSignature(sootMethod),
                    convertAnnotations(sootMethod),
                    convertParamAnnotations(sootMethod),
                    convertParamNames(sootMethod),
                    sootMethod
            );
        });
    }

    FieldRef convertFieldRef(SootFieldRef sootFieldRef) {
        return fieldRefMap.computeIfAbsent(sootFieldRef, ref -> {
            JClass cls = convertClass(ref.declaringClass());
            Type type = convertType(ref.type());
            return FieldRef.get(cls, ref.name(), type, ref.isStatic());
        });
    }

    MethodRef convertMethodRef(SootMethodRef sootMethodRef) {
        return methodRefMap.computeIfAbsent(sootMethodRef, ref -> {
            JClass cls = convertClass(ref.getDeclaringClass());
            List<Type> paramTypes = Lists.map(
                    ref.getParameterTypes(), this::convertType);
            Type returnType = convertType(ref.getReturnType());
            return MethodRef.get(cls, ref.getName(), paramTypes, returnType,
                    ref.isStatic());
        });
    }

    /**
     * @return the signature attribute for dealing with generics
     *         starting from Java 1.5.
     * @see ReferenceTypeGSignature
     */
    @Nullable
    private static ReferenceTypeGSignature convertGSignature(SootField sootField) {
        Tag tag = sootField.getTag("SignatureTag");
        if (tag instanceof SignatureTag signatureTag) {
            return GSignatures.toTypeSig(signatureTag.getSignature());
        }
        return null;
    }

    /**
     * @return the signature attribute for dealing with generics
     *         starting from Java 1.5.
     * @see MethodGSignature
     */
    @Nullable
    private static MethodGSignature convertGSignature(SootMethod sootMethod) {
        Tag tag = sootMethod.getTag("SignatureTag");
        if (tag instanceof SignatureTag signatureTag) {
            return GSignatures.toMethodSig(signatureTag.getSignature());
        }
        return null;
    }

    /**
     * @return an annotation holder that contains all annotations in {@code host}.
     * @see AbstractHost
     */
    static AnnotationHolder convertAnnotations(AbstractHost host) {
        var tag = (VisibilityAnnotationTag) host.getTag(VisibilityAnnotationTag.NAME);
        return convertAnnotations(tag);
    }

    /**
     * @return an annotation holder that contains all annotations in {@code tag}.
     * @see VisibilityAnnotationTag
     */
    private static AnnotationHolder convertAnnotations(
            @Nullable VisibilityAnnotationTag tag) {
        // in Soot, each VisibilityAnnotationTag may contain multiple annotations
        // (named AnnotationTag, which is a bit confusing).
        return tag == null || tag.getAnnotations() == null ?
                AnnotationHolder.emptyHolder() :
                // converts all annotations in tag
                AnnotationHolder.make(Lists.map(tag.getAnnotations(),
                        Converter::convertAnnotation));
    }

    private static Annotation convertAnnotation(AnnotationTag tag) {
        // AnnotationTag is the class that represent an annotation in Soot
        String annotationType = StringReps.toTaieTypeDesc(tag.getType());
        Map<String, Element> elements = Maps.newHybridMap();
        // converts all elements in tag
        tag.getElems().forEach(e -> {
            String name = e.getName();
            Element elem = convertAnnotationElement(e);
            elements.put(name, elem);
        });
        return new Annotation(annotationType, elements);
    }

    private static Element convertAnnotationElement(AnnotationElem elem) {
        if (elem instanceof AnnotationStringElem e) {
            return new StringElement(e.getValue());
        } else if (elem instanceof AnnotationClassElem e) {
            String className = e.getDesc();
            // Soot's .java front end has different representation from .class
            // front end for AnnotationClassElem, and here we need to remove
            // extra characters generated by .java frontend
            int iBracket = className.indexOf('<');
            if (iBracket != -1) {
                className = className.replace("java/lang/Class<", "")
                        .replace(">", "");
            }
            return new ClassElement(StringReps.toTaieTypeDesc(className));
        } else if (elem instanceof AnnotationAnnotationElem e) {
            return new AnnotationElement(convertAnnotation(e.getValue()));
        } else if (elem instanceof AnnotationArrayElem e) {
            return new ArrayElement(Lists.map(e.getValues(),
                    Converter::convertAnnotationElement));
        } else if (elem instanceof AnnotationEnumElem e) {
            return new EnumElement(
                    StringReps.toTaieTypeDesc(e.getTypeName()),
                    e.getConstantName());
        } else if (elem instanceof AnnotationIntElem e) {
            return new IntElement(e.getValue());
        } else if (elem instanceof AnnotationBooleanElem e) {
            return new BooleanElement(e.getValue());
        } else if (elem instanceof AnnotationFloatElem e) {
            return new FloatElement(e.getValue());
        } else if (elem instanceof AnnotationDoubleElem e) {
            return new DoubleElement(e.getValue());
        } else if (elem instanceof AnnotationLongElem e) {
            return new LongElement(e.getValue());
        } else {
            throw new SootFrontendException(
                    "Unable to handle AnnotationElem: " + elem);
        }
    }

    /**
     * Converts all annotations of parameters of {@code sootMethod} to a list
     * of {@link AnnotationHolder}, one for annotations of each parameter.
     *
     * @see VisibilityParameterAnnotationTag
     */
    @Nullable
    private static List<AnnotationHolder> convertParamAnnotations(
            SootMethod sootMethod) {
        // in Soot, each VisibilityParameterAnnotationTag contains
        // the annotations for all parameters in the SootMethod
        var tag = (VisibilityParameterAnnotationTag)
                sootMethod.getTag(VisibilityParameterAnnotationTag.NAME);
        return tag == null ? null :
                Lists.map(tag.getVisibilityAnnotations(), Converter::convertAnnotations);
    }

    /**
     * Converts all names of parameters of {@code sootMethod} to a list.
     *
     * @see ParamNamesTag
     */
    @Nullable
    private static List<String> convertParamNames(
            SootMethod sootMethod) {
        // in Soot, each ParamNamesTag contains the names of all parameters in the SootMethod
        var tag = (ParamNamesTag) sootMethod.getTag(ParamNamesTag.NAME);
        return tag == null || tag.getNames().isEmpty() ? null : tag.getNames();
    }
}
