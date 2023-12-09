


package keeno.usap.language.generics;

import keeno.usap.util.Experimental;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-TypeArgument">
 * JVM Spec. 4.7.9.1 TypeArgument</a>
 */
public final class TypeArgument implements Serializable {

    private static final TypeArgument ALL =
            new TypeArgument(Kind.ALL, null);

    private final Kind kind;

    @Nullable
    private final ReferenceTypeGSignature gSig;

    private TypeArgument(Kind kind,
                         @Nullable ReferenceTypeGSignature gSig) {
        this.kind = kind;
        this.gSig = gSig;
    }

    @Experimental
    public Kind getKind() {
        return kind;
    }

    @Nullable
    @Experimental
    public ReferenceTypeGSignature getGSignature() {
        return gSig;
    }

    public static TypeArgument all() {
        return ALL;
    }

    public static TypeArgument of(char symbol,
                                  @Nonnull ReferenceTypeGSignature gSig) {
        Kind kind = Kind.of(symbol);
        assert kind != Kind.ALL && gSig != null;
        return new TypeArgument(kind, gSig);
    }

    @Override
    public String toString() {
        return switch (kind) {
            case INSTANCEOF -> gSig.toString();
            case ALL -> "?";
            case EXTENDS, SUPER -> "? " + kind.name().toLowerCase() + " " + gSig;
        };
    }

    public enum Kind {

        ALL('*'),

        INSTANCEOF(org.objectweb.asm.signature.SignatureVisitor.INSTANCEOF),
        EXTENDS(org.objectweb.asm.signature.SignatureVisitor.EXTENDS),
        SUPER(org.objectweb.asm.signature.SignatureVisitor.SUPER);

        private final char symbol;

        private Kind(char symbol) {
            this.symbol = symbol;
        }

        public static Kind of(char symbol) {
            for (Kind indicator : Kind.values()) {
                if (indicator.symbol == symbol) {
                    return indicator;
                }
            }
            throw new IllegalArgumentException("Unknown wildcard indicator: " + symbol);
        }

    }

}
