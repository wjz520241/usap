

package keeno.usap.language.annotation;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注解的容器
 * 这个类使 {@link Annotated}类很容易实现。
 */
public class AnnotationHolder implements Serializable {

    /**
     *从注解类型映射到此持有者中的相应注解。
     */
    private final Map<String, Annotation> annotations;

    protected AnnotationHolder(Collection<Annotation> annotations) {
        this.annotations = annotations.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Annotation::getType, a -> a));
    }

    public boolean hasAnnotation(String annotationType) {
        return annotations.containsKey(annotationType);
    }

    @Nullable
    public Annotation getAnnotation(String annotationType) {
        return annotations.get(annotationType);
    }

    public Collection<Annotation> getAnnotations() {
        return annotations.values();
    }

    private static final AnnotationHolder EMPTY_HOLDER = new AnnotationHolder(Set.of());

    /**
     * Creates an annotation holder that contains the annotations
     * in given collection.
     */
    public static AnnotationHolder make(Collection<Annotation> annotations) {
        return annotations.isEmpty() ? EMPTY_HOLDER : new AnnotationHolder(annotations);
    }

    /**
     * @return an annotation holder that contains no annotations.
     */
    public static AnnotationHolder emptyHolder() {
        return EMPTY_HOLDER;
    }
}
