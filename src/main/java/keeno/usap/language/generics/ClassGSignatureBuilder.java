


package keeno.usap.language.generics;

import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link ClassGSignature}.
 */
final class ClassGSignatureBuilder extends TypeParameterAwareGSignatureBuilder {

    private final boolean isInterface;

    private ClassGSignature gSig;

    private ClassTypeGSignature superClass;

    private List<ClassTypeGSignature> interfaces = new ArrayList<>();

    public ClassGSignatureBuilder(boolean isInterface) {
        this.isInterface = isInterface;
    }

    public ClassGSignature get() {
        endInterface();
        endSuperClass();
        if (gSig == null) {
            gSig = new ClassGSignature(isInterface, typeParams,
                    superClass, interfaces);
        }
        return gSig;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        endInterfaceBound();
        endClassBound();
        endTypeParam();
        stack.push(State.VISIT_SUPERCLASS);
        return newTypeGSignatureBuilder();
    }

    /**
     * The valid automaton states for ending a {@link #visitSuperclass()} method call are:
     * <ul>
     *     <li>{@link #visitInterface()}</li>
     *     <li>{@link #get()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the superclass.
     */
    private void endSuperClass() {
        if (stack.peek() == State.VISIT_SUPERCLASS) {
            stack.pop();
            superClass = getTypeGSignature();
        }
    }

    @Override
    public SignatureVisitor visitInterface() {
        endInterface();
        endSuperClass();
        stack.push(State.VISIT_INTERFACE);
        return newTypeGSignatureBuilder();
    }

    /**
     * The valid automaton states for ending a {@link #visitInterface()} method call are:
     * <ul>
     *     <li>{@link #visitInterface()}</li>
     *     <li>{@link #get()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the interface.
     */
    private void endInterface() {
        if (stack.peek() == State.VISIT_INTERFACE) {
            stack.pop();
            interfaces.add(getTypeGSignature());
        }
    }

}
