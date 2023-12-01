package keeno.usap.ir.exp;


import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.language.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 参阅doc目录中的《方法调用图》
 * 调用动态指令的表示。有关invokedynamic指令的更多详细信息，请参阅
 *https://docs.oracle.com/javase/7/docs/api/java/lang/invoke/package-summary.html
 */
public class InvokeDynamic extends InvokeExp {

    private final MethodRef bootstrapMethodRef;

    private final String methodName;

    private final MethodType methodType;

    /**
     * 引导程序方法的其他静态参数。由于所有这些参数都取自常量池，因此我们将它们存储为Literals列表。
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

}
