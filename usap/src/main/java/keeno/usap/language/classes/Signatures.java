

package keeno.usap.language.classes;

/**
 * Provides signatures of special methods and fields.
 */
@StringProvider
public final class Signatures {

    // Signatures of special methods
    public static final String FINALIZE = "<java.lang.Object: void finalize()>";

    public static final String FINALIZER_REGISTER = "<java.lang.ref.Finalizer: void register(java.lang.Object)>";

    public static final String REFERENCE_INIT = "<java.lang.ref.Reference: void <init>(java.lang.Object,java.lang.ref.ReferenceQueue)>";

    public static final String LAMBDA_METAFACTORY = "<java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>";

    public static final String LAMBDA_ALTMETAFACTORY = "<java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite altMetafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.Object[])>";

    public static final String STRING_CONCAT_FACTORY_MAKE = "<java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>";

    public static final String INVOKEDYNAMIC_FINDVIRTUAL = "<java.lang.invoke.MethodHandles$Lookup: java.lang.invoke.MethodHandle findVirtual(java.lang.Class,java.lang.String,java.lang.invoke.MethodType)>";

    public static final String INVOKEDYNAMIC_FINDSTATIC = "<java.lang.invoke.MethodHandles$Lookup: java.lang.invoke.MethodHandle findStatic(java.lang.Class,java.lang.String,java.lang.invoke.MethodType)>";

    public static final String INVOKEDYNAMIC_FINDSPECIAL = "<java.lang.invoke.MethodHandles$Lookup: java.lang.invoke.MethodHandle findSpecial(java.lang.Class,java.lang.String,java.lang.invoke.MethodType,java.lang.Class)>";

    public static final String INVOKEDYNAMIC_FINDCONSTRUCTOR = "<java.lang.invoke.MethodHandles$Lookup: java.lang.invoke.MethodHandle findConstructor(java.lang.Class,java.lang.invoke.MethodType)>";

    // Signatures of special fields
    public static final String REFERENCE_PENDING = "<java.lang.ref.Reference: java.lang.ref.Reference pending>";

    // Suppresses default constructor, ensuring non-instantiability.
    private Signatures() {
    }
}
