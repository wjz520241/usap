

package keeno.usap.language.annotation;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * 表示可以附加注解的对象。
 * <p>
 * 目前，只有{@code JClass}、{@code JMethod}和{@code JField}
 * 实现了这个接口。此外，还支持对参数进行注解，并且它们存储在{@code JMethod}中，而不是IR中的参数。
 * <p>
 * TODO: let other program elements (e.g., {@code Var} implements this interface.
 */
public interface Annotated {

    /**
     * @return {@code true} if this annotated object has an annotation
     * of {@code annotationType}.
     */
    boolean hasAnnotation(String annotationType);

    /**
     * @return the {@link Annotation} of type {@code annotationType} if
     * it is present in this annotated; otherwise, {@code null} is returned.
     */
    @Nullable
    Annotation getAnnotation(String annotationType);

    /**
     * @return all annotations in this annotated object.
     */
    Collection<Annotation> getAnnotations();
}
