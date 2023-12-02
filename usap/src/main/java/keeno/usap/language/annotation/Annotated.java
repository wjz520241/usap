package keeno.usap.language.annotation;

import javax.annotation.Nullable;
import java.util.Collection;

public interface Annotated {
    boolean hasAnnotation(String annotationType);

    @Nullable
    Annotation getAnnotation(String annotationType);

    Collection<Annotation> getAnnotations();
}
