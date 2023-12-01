
package keeno.usap.ir.exp;


import keeno.usap.language.ClassType;
import keeno.usap.language.Type;

import java.util.List;


public class MethodType implements ReferenceLiteral {

    private final List<Type> paramTypes;

    private final Type returnType;

    private MethodType(List<Type> paramTypes, Type returnType) {
        this.paramTypes = List.copyOf(paramTypes);
        this.returnType = returnType;
    }

    public static MethodType get(List<Type> paramTypes, Type returnType) {
        return new MethodType(paramTypes, returnType);
    }

    public List<Type> getParamTypes() {
        return paramTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public ClassType getType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MethodType that = (MethodType) o;
        return paramTypes.equals(that.paramTypes) &&
                returnType.equals(that.returnType);
    }
}
