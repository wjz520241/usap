

/**
 * In this package, we offer a generics model for Java.
 * It is not an invasive change to the type system, but rather an additional attribute which
 * offers information about the generics starting from Java 5,
 * as in <a href="https://docs.oracle.com/javase/specs/jvms/se20/html/jvms-4.html#jvms-4.7.9.1">
 * JVM Spec. 4.7.9.1 Signatures</a>.
 * Notes that our implementation will not follow the JVM Spec. strictly, and will
 * be altered slightly for convenience, e.g.,
 * <ul>
 *     <li>We name <i>*Signature</i> as <i>*GSignature</i> (<i>G</i> means <i>Generics</i>).</li>
 *     <li>We name <i>JavaTypeSignature</i> as <i>TypeGSignature</i>.</li>
 *     <li>
 *         We make <i>{@link keeno.usap.language.generics.VoidDescriptor}</i>
 *         a <i>{@link keeno.usap.language.generics.TypeGSignature}</i>.
 *     </li>
 * </ul>
 */
@Experimental
package keeno.usap.language.generics;

import keeno.usap.util.Experimental;
