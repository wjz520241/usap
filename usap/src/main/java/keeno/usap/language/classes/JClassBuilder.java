

package keeno.usap.language.classes;

import keeno.usap.language.annotation.AnnotationHolder;
import keeno.usap.language.generics.ClassGSignature;
import keeno.usap.language.type.ClassType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * Each JClassBuilder builds one JClass.
 * TODO: make the relation between JClassBuilder and JClass explicit.
 */
public interface JClassBuilder {

    void build(JClass jclass);

    Set<Modifier> getModifiers();

    String getSimpleName();

    ClassType getClassType();

    JClass getSuperClass();

    Collection<JClass> getInterfaces();

    JClass getOuterClass();

    Collection<JField> getDeclaredFields();

    Collection<JMethod> getDeclaredMethods();

    AnnotationHolder getAnnotationHolder();

    /**
     * @return true if this class is application class, otherwise false.
     */
    boolean isApplication();

    /**
     * @return true if this class is phantom class, otherwise false.
     */
    boolean isPhantom();

    /**
     * @return the signature attribute for dealing with generics
     * starting from Java 1.5.
     * @see ClassGSignature
     */
    @Nullable
    ClassGSignature getGSignature();
}
