


package keeno.usap.language.generics;

import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static keeno.usap.language.generics.ClassTypeGSignature.JAVA_LANG;
import static keeno.usap.language.generics.ClassTypeGSignature.JAVA_LANG_OBJECT;
import static keeno.usap.language.generics.ClassTypeGSignature.OBJECT;

/**
 * Builds a {@link TypeGSignature}.
 */
class TypeGSignatureBuilder extends SignatureVisitor {

    private final Deque<State> stack = new ArrayDeque<>();

    private TypeGSignatureBuilder typeGSigBuilder;

    private TypeGSignature result;

    // Base Type related data

    private Character baseTypeDesc;

    // Type Variable Signature related data

    private String typeName;

    // Array Type Signature related data

    private int arrayDimension = 0;

    // Class Type Signature related data

    private String packageName;

    private List<String> classNames = new ArrayList<>();

    private List<List<TypeArgument>> typeArgLists = new ArrayList<>();

    private Character typeArgWildcard;

    TypeGSignatureBuilder() {
        super(GSignatures.API);
    }

    public TypeGSignature get() {
        if (result == null) {
            State state = stack.pop();
            result = switch (state) {
                case VISIT_BASE_TYPE -> VoidDescriptor.isVoid(baseTypeDesc)
                        ? VoidDescriptor.VOID : BaseType.of(baseTypeDesc);
                case VISIT_TYPE_VARIABLE -> TypeVariableGSignature.of(typeName);
                case VISIT_CLASS_TYPE -> buildClassTypeGSignature();
                default -> throw new IllegalStateException("Unexpected state: " + state);
            };
            if (arrayDimension > 0) {
                result = new ArrayTypeGSignature(arrayDimension, result);
                arrayDimension = 0;
            }
        }
        return result;
    }

    @Override
    public void visitBaseType(char descriptor) {
        stack.push(State.VISIT_BASE_TYPE);
        baseTypeDesc = descriptor;
    }

    @Override
    public void visitTypeVariable(String name) {
        stack.push(State.VISIT_TYPE_VARIABLE);
        typeName = name;
    }

    @Override
    public SignatureVisitor visitArrayType() {
        arrayDimension++;
        return this;
    }

    @Override
    public void visitClassType(String name) {
        stack.push(State.VISIT_CLASS_TYPE);
        int index = name.lastIndexOf('/');
        if (index != -1) {
            packageName = name.substring(0, index).replace('/', '.');
            name = name.substring(index + 1);
        }
        addClassName(name);
    }

    @Override
    public void visitInnerClassType(String name) {
        endTypeArg();
        addClassName(name);
    }

    @Override
    public void visitTypeArgument() {
        addTypeArg(TypeArgument.all());
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        endTypeArg();
        stack.push(State.VISIT_TYPE_ARGUMENT);
        typeArgWildcard = wildcard;
        typeGSigBuilder = new TypeGSignatureBuilder();
        return typeGSigBuilder;
    }

    @Override
    public void visitEnd() {
        endTypeArg();
    }

    /**
     * The valid automaton states for ending a {@link #visitTypeArgument(char)} method call are:
     * <ul>
     *     <li>{@link #visitTypeArgument(char)}</li>
     *     <li>{@link #visitInnerClassType(String)}</li>
     *     <li>{@link #visitEnd()}</li>
     * </ul>
     * When the automaton state is in one of these above states,
     * it is time to collect the type argument.
     */
    private void endTypeArg() {
        if (stack.peek() == State.VISIT_TYPE_ARGUMENT) {
            stack.pop();
            assert typeGSigBuilder != null;
            TypeGSignature gSig = typeGSigBuilder.get();
            typeGSigBuilder = null;
            assert gSig instanceof ReferenceTypeGSignature;
            assert typeArgWildcard != null;
            var typeArg = TypeArgument.of(typeArgWildcard,
                    (ReferenceTypeGSignature) gSig);
            typeArgWildcard = null;
            addTypeArg(typeArg);
        }
    }

    // Utility methods

    private void addClassName(String className) {
        classNames.add(className);
        if (typeArgLists.size() != classNames.size()) {
            typeArgLists.add(new ArrayList<>());
        }
    }

    private void addTypeArg(TypeArgument typeArg) {
        typeArgLists.get(typeArgLists.size() - 1)
                .add(typeArg);
    }

    private ClassTypeGSignature buildClassTypeGSignature() {
        try {
            if (JAVA_LANG.equals(packageName)
                    && classNames.size() == 1
                    && OBJECT.equals(classNames.get(0))) {
                return JAVA_LANG_OBJECT;
            }
            assert classNames != null && !classNames.isEmpty();
            List<ClassTypeGSignature.SimpleClassTypeGSignature> typeSigs = new ArrayList<>();
            for (int i = 0; i < classNames.size(); i++) {
                typeSigs.add(new ClassTypeGSignature.SimpleClassTypeGSignature(
                        classNames.get(i),
                        List.copyOf(typeArgLists.get(i))
                ));
            }
            return new ClassTypeGSignature(packageName,
                    List.copyOf(typeSigs));
        } finally {
            packageName = null;
            classNames.clear();
            typeArgLists.clear();
        }
    }

    private enum State {

        VISIT_BASE_TYPE,

        VISIT_TYPE_VARIABLE,

        VISIT_CLASS_TYPE,

        VISIT_TYPE_ARGUMENT,

        ;
    }
}
