

package keeno.usap.language.classes;

import keeno.usap.language.annotation.Annotated;
import keeno.usap.language.annotation.Annotation;
import keeno.usap.language.annotation.AnnotationHolder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public abstract class ClassMember implements Annotated, Serializable {

    protected final JClass declaringClass;

    protected final String name;

    protected final Set<Modifier> modifiers;

    protected final AnnotationHolder annotationHolder;

    protected String signature;

    // TODO: source location

    protected ClassMember(JClass declaringClass, String name,
                          Set<Modifier> modifiers,
                          AnnotationHolder annotationHolder) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.modifiers = modifiers;
        this.annotationHolder = annotationHolder;
    }

    /**
     * @return the declaring class of the class member.
     */
    public JClass getDeclaringClass() {
        return declaringClass;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public boolean isPublic() {
        return Modifier.hasPublic(modifiers);
    }

    public boolean isProtected() {
        return Modifier.hasProtected(modifiers);
    }

    public boolean isPrivate() {
        return Modifier.hasPrivate(modifiers);
    }

    public boolean isStatic() {
        return Modifier.hasStatic(modifiers);
    }

    public boolean isApplication() {
        return declaringClass.isApplication();
    }

    @Override
    public boolean hasAnnotation(String annotationType) {
        return annotationHolder.hasAnnotation(annotationType);
    }

    @Override
    @Nullable
    public Annotation getAnnotation(String annotationType) {
        return annotationHolder.getAnnotation(annotationType);
    }

    @Override
    public Collection<Annotation> getAnnotations() {
        return annotationHolder.getAnnotations();
    }

    @Override
    public String toString() {
        return signature;
    }
}
