

package keeno.usap.language.classes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marker annotation for the classes that provide string representations
 * of special program elements (e.g., names and signatures) via final fields.
 */
@Target(ElementType.TYPE)
@interface StringProvider {
}
