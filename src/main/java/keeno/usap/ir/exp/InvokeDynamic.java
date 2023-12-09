

package keeno.usap.ir.exp;

import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.language.type.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Representation of invokedynamic instructions.
 * For more details about invokedynamic instructions, please refer to
 * https://docs.oracle.com/javase/7/docs/api/java/lang/invoke/package-summary.html
 */
public class InvokeDynamic extends InvokeExp {

    private final MethodRef bootstrapMethodRef;

    private final String methodName;

    private final MethodType methodType;

    /**
     * Additional static arguments for bootstrap method.
     * As all these arguments are taken from the constant pool,
     * we store them as a list of Literals.
     */
    private final List<Literal> bootstrapArgs;

    public InvokeDynamic(MethodRef bootstrapMethodRef,
                         String methodName, MethodType methodType,
                         List<Literal> bootstrapArgs, List<Var> args) {
        super(null, args);
        this.bootstrapMethodRef = bootstrapMethodRef;
        this.methodName = methodName;
        this.methodType = methodType;
        this.bootstrapArgs = List.copyOf(bootstrapArgs);
    }

    public MethodRef getBootstrapMethodRef() {
        return bootstrapMethodRef;
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public List<Literal> getBootstrapArgs() {
        return bootstrapArgs;
    }

    @Override
    public Type getType() {
        return methodType.getReturnType();
    }

    @Override
    public MethodRef getMethodRef() {
        throw new UnsupportedOperationException(
                "InvokeDynamic.getMethodRef() is unavailable");
    }

    @Override
    public String getInvokeString() {
        return "invokedynamic";
    }

    @Override
    public String toString() {
        return String.format("%s %s \"%s\" <%s>[%s]%s",
                getInvokeString(), bootstrapMethodRef,
                methodName, methodType,
                bootstrapArgs.stream()
                        .map(Literal::toString)
                        .collect(Collectors.joining(",")),
                getArgsString());
    }

    @Override
    public <T> T accept(ExpVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
