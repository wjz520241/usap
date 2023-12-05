package keeno.usap.language.generics;


import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 收集包含类型参数的签名。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-TypeParameter">
 * JVM Spec. 4.7.9.1 TypeParameter</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-ClassSignature">
 * JVM Spec. 4.7.9.1 ClassSignature</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-MethodSignature">
 * JVM Spec. 4.7.9.1 MethodSignature</a>
 */
abstract class TypeParameterAwareGSignatureBuilder extends SignatureVisitor {

    protected final Deque<State> stack = new ArrayDeque<>();

    protected List<TypeParameter> typeParams = new ArrayList<>();

    private TypeGSignatureBuilder typeGSigBuilder;

    // Type parameter related data

    private String typeName;

    private ReferenceTypeGSignature classBound;

    private List<ReferenceTypeGSignature> interfaceBounds = new ArrayList<>();

    TypeParameterAwareGSignatureBuilder() {
        super(GSignatures.API);
    }

    // -------------------------------------------------------------------------------------
    // Class/Method signature common related methods
    // -------------------------------------------------------------------------------------

    @Override
    public void visitFormalTypeParameter(String name) {
        endInterfaceBound();
        endClassBound();
        endTypeParam();
        stack.push(State.VISIT_TYPE_PARAM);
        typeName = name;
    }

    /**
     * The valid automaton states for ending a {@link #visitFormalTypeParameter(String)} method call are:
     * <ul>
     *     <li>{@link #visitFormalTypeParameter(String)}</li>
     *     <li>{@link #visitSuperclass()}</li>
     *     <li>{@link #visitParameterType()}</li>
     *     <li>{@link #visitReturnType()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the current type parameter.
     */
    protected void endTypeParam() {
        if (stack.peek() == State.VISIT_TYPE_PARAM) {
            stack.pop();
            typeParams.add(buildTypeParameter());
        }
    }

    @Override
    public SignatureVisitor visitClassBound() {
        stack.push(State.VISIT_CLASS_BOUND);
        return newTypeGSignatureBuilder();
    }

    /**
     * The valid automaton states for ending a {@link #visitClassBound()} method call are:
     * <ul>
     *     <li>{@link #visitFormalTypeParameter(String)}</li>
     *     <li>{@link #visitInterfaceBound()}</li>
     *     <li>{@link #visitSuperclass()}</li>
     *     <li>{@link #visitParameterType()}</li>
     *     <li>{@link #visitReturnType()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the class bound of current type parameter.
     */
    protected void endClassBound() {
        if (stack.peek() == State.VISIT_CLASS_BOUND) {
            stack.pop();
            classBound = getTypeGSignature();
        }
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        endInterfaceBound();
        endClassBound();
        stack.push(State.VISIT_INTERFACE_BOUND);
        return newTypeGSignatureBuilder();
    }

    /**
     * The valid automaton states for ending a {@link #visitInterfaceBound()} method call are:
     * <ul>
     *     <li>{@link #visitFormalTypeParameter(String)}</li>
     *     <li>{@link #visitInterfaceBound()}</li>
     *     <li>{@link #visitSuperclass()}</li>
     *     <li>{@link #visitParameterType()}</li>
     *     <li>{@link #visitReturnType()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the interface bound of current type parameter.
     */
    protected void endInterfaceBound() {
        if (stack.peek() == State.VISIT_INTERFACE_BOUND) {
            stack.pop();
            interfaceBounds.add(getTypeGSignature());
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Utility methods
    // -----------------------------------------------------------------------------------------------

    protected TypeGSignatureBuilder newTypeGSignatureBuilder() {
        assert typeGSigBuilder == null;
        typeGSigBuilder = new TypeGSignatureBuilder();
        return typeGSigBuilder;
    }

    @SuppressWarnings("unchecked")
    protected <T extends TypeGSignature> T getTypeGSignature() {
        assert typeGSigBuilder != null;
        T result = (T) typeGSigBuilder.get();
        typeGSigBuilder = null;
        return result;
    }

    private TypeParameter buildTypeParameter() {
        try {
            assert typeName != null;
            if (classBound == ClassTypeGSignature.JAVA_LANG_OBJECT
                    && interfaceBounds.isEmpty()
                    && (TypeParameter.T.getTypeName().equals(typeName) || TypeParameter.E.getTypeName().equals(typeName))) {
                return TypeParameter.T.getTypeName().equals(typeName) ? TypeParameter.T : TypeParameter.E;
            }
            return new TypeParameter(typeName,
                    classBound, List.copyOf(interfaceBounds));
        } finally {
            typeName = null;
            classBound = null;
            interfaceBounds.clear();
        }
    }

    protected enum State {

        VISIT_TYPE_PARAM,

        VISIT_CLASS_BOUND,

        VISIT_INTERFACE_BOUND,

        VISIT_SUPERCLASS,

        VISIT_INTERFACE,

        VISIT_PARAMETER_TYPE,

        VISIT_RETURN_TYPE,

        VISIT_EXCEPTION_TYPE;
    }

}
