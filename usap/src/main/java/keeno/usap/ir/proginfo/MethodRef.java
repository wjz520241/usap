package keeno.usap.ir.proginfo;

import keeno.usap.language.Type;

import java.util.List;

public class MethodRef extends MemberRef {

    private final List<Type> parameterTypes;

    private final Type returnType;

    public MethodRef(String name, List<Type> parameterTypes, Type returnType,
                     boolean isStatic) {
        super(name, isStatic);
        this.parameterTypes = List.copyOf(parameterTypes);
        this.returnType = returnType;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

}
