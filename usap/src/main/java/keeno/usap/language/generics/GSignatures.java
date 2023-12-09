


package keeno.usap.language.generics;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import keeno.usap.util.Experimental;

import javax.annotation.Nullable;

/**
 * Utility methods for converting signatures.
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-4.7.9.1">
 * JVM Spec. 4.7.9.1 Signatures</a>
 */
public final class GSignatures {

    private GSignatures() {
    }

    /**
     * The ASM API version implemented by this visitor.
     */
    public static final int API = Opcodes.ASM9;

    @Nullable
    @Experimental
    public static ClassGSignature toClassSig(boolean isInterface, String sig) {
        var builder = new ClassGSignatureBuilder(isInterface);
        new SignatureReader(sig).accept(builder);
        ClassGSignature gSig = builder.get();
        return gSig;
    }

    @Nullable
    @Experimental
    public static MethodGSignature toMethodSig(String sig) {
        var builder = new MethodGSignatureBuilder();
        new SignatureReader(sig).accept(builder);
        MethodGSignature gSig = builder.get();
        return gSig;
    }

    @Nullable
    @Experimental
    @SuppressWarnings("unchecked")
    public static <T extends TypeGSignature> T toTypeSig(String sig) {
        var builder = new TypeGSignatureBuilder();
        new SignatureReader(sig).accept(builder);
        TypeGSignature gSig = builder.get();
        return (T) gSig;
    }

}
