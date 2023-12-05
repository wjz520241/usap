package keeno.usap.language.generics;

import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link MethodGSignature}.
 */
final class MethodGSignatureBuilder extends TypeParameterAwareGSignatureBuilder {

    private MethodGSignature gSig;

    private List<TypeGSignature> params = new ArrayList<>();

    private TypeGSignature returnType;

    private List<TypeGSignature> exceptionTypes = new ArrayList<>();

    public MethodGSignature get() {
        endExceptionType();
        endReturnType();
        if (gSig == null) {
            gSig = new MethodGSignature(typeParams, params,
                    returnType, exceptionTypes);
        }
        return gSig;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        endInterfaceBound();
        endClassBound();
        endTypeParam();
        endParameterType();
        stack.push(State.VISIT_PARAMETER_TYPE);
        return newTypeGSignatureBuilder();
    }

    /**
     * The valid automaton states for ending a {@link #visitParameterType()} method call are:
     * <ul>
     *     <li>{@link #visitParameterType()}</li>
     *     <li>{@link #visitReturnType()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the parameter type.
     */
    private void endParameterType() {
        if (stack.peek() == State.VISIT_PARAMETER_TYPE) {
            stack.pop();
            params.add(getTypeGSignature());
        }
    }

    @Override
    public SignatureVisitor visitReturnType() {
        endInterfaceBound();
        endClassBound();
        endTypeParam();
        endParameterType();
        stack.push(State.VISIT_RETURN_TYPE);
        return newTypeGSignatureBuilder();
    }

    /**
     * The valid automaton states for ending a {@link #visitReturnType()} method call are:
     * <ul>
     *     <li>{@link #visitExceptionType()}</li>
     *     <li>{@link #get()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the return type.
     */
    private void endReturnType() {
        if (stack.peek() == State.VISIT_RETURN_TYPE) {
            stack.pop();
            returnType = getTypeGSignature();
        }
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        endReturnType();
        endExceptionType();
        stack.push(State.VISIT_EXCEPTION_TYPE);
        return newTypeGSignatureBuilder();
    }

    /**
     * The valid automaton states for ending a {@link #visitExceptionType()} method call are:
     * <ul>
     *     <li>{@link #visitExceptionType()}</li>
     *     <li>{@link #get()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the exception type.
     */
    private void endExceptionType() {
        if (stack.peek() == State.VISIT_EXCEPTION_TYPE) {
            stack.pop();
            exceptionTypes.add(getTypeGSignature());
        }
    }

}
