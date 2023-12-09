

package keeno.usap.analysis.pta.plugin.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for conveniently marking Invoke handlers in API models.
 */
@Documented
@Repeatable(InvokeHandlers.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface InvokeHandler {

    /**
     * @return method signature of the Invoke to he handled.
     */
    String signature();

    /**
     * @return an array of indexes for arguments used by the handler.
     */
    int[] argIndexes() default {};
}
