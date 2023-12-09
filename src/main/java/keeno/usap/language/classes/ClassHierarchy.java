

package keeno.usap.language.classes;

import keeno.usap.ir.proginfo.FieldRef;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.language.type.Type;
import keeno.usap.util.Indexer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Manages the classes and class-related resolution of the program being analyzed.
 */
public interface ClassHierarchy extends Indexer<JClass> {

    void setDefaultClassLoader(JClassLoader loader);

    JClassLoader getDefaultClassLoader();

    void setBootstrapClassLoader(JClassLoader loader);

    JClassLoader getBootstrapClassLoader();

    Collection<JClassLoader> getClassLoaders();

    /**
     * Adds a JClass into class hierarchy.
     * This API should be invoked everytime {@link JClassLoader}
     * loads a new JClass.
     */
    void addClass(JClass jclass);

    Stream<JClass> allClasses();

    Stream<JClass> applicationClasses();

    @Nullable
    JClass getClass(JClassLoader loader, String name);

    @Nullable
    JClass getClass(String name);

    /**
     * Obtains a method by its signature.
     *
     * @param methodSig of the method
     * @return the {@link JMethod} for signature if found;
     * otherwise, null.
     * @throws keeno.usap.util.AnalysisException if signature is invalid.
     */
    @Nullable
    JMethod getMethod(String methodSig);

    /**
     * Obtains a field by its signature.
     *
     * @param fieldSig signature of the field
     * @return the {@link JField} for signature if found;
     * otherwise, null.
     * @throws keeno.usap.util.AnalysisException if signature is invalid.
     */
    @Nullable
    JField getField(String fieldSig);

    /**
     * Obtains a JRE class by it name.
     *
     * @param name the class name
     * @return the {@link JClass} for name if found;
     * otherwise, null.
     */
    @Nullable
    JClass getJREClass(String name);

    /**
     * Obtains a method declared in a JRE class by its signature.
     *
     * @param methodSig of the method
     * @return the {@link JMethod} for signature if found;
     * otherwise, null.
     * @throws keeno.usap.util.AnalysisException if signature is invalid.
     */
    @Nullable
    JMethod getJREMethod(String methodSig);

    /**
     * Obtains a field declared in a JRE class by its signature.
     *
     * @param fieldSig signature of the field
     * @return the {@link JField} for signature if found;
     * otherwise, null.
     * @throws keeno.usap.util.AnalysisException if signature is invalid.
     */
    @Nullable
    JField getJREField(String fieldSig);

    /**
     * Resolves a method reference.
     *
     * @return the concrete method pointed by the method reference,
     * or null if the concrete method cannot be found in the class hierarchy.
     */
    @Nullable
    JMethod resolveMethod(MethodRef methodRef);

    /**
     * Resolves a field reference.
     *
     * @return the concrete field pointed by the field reference,
     * or null if the concrete field cannot be found in the class hierarchy.
     */
    @Nullable
    JField resolveField(FieldRef fieldRef);

    /**
     * Dispatches a method reference on a receiver type.
     *
     * @return the target method. If the target cannot be found, returns null.
     * @throws keeno.usap.util.AnalysisException if given receiver type
     *                                            cannot be dispatched (e.g., given a primitive type).
     */
    @Nullable
    JMethod dispatch(Type receiverType, MethodRef methodRef);

    /**
     * Dispatches a method reference on a receiver class.
     *
     * @return the target method. If the target cannot be found, returns null.
     */
    @Nullable
    JMethod dispatch(JClass receiverClass, MethodRef methodRef);

    /**
     * @return the direct subinterfaces of given interface.
     */
    Collection<JClass> getDirectSubinterfacesOf(JClass jclass);

    /**
     * @return the direct implementors of given interface.
     */
    Collection<JClass> getDirectImplementorsOf(JClass jclass);

    /**
     * @return the direct subclasses of given class.
     */
    Collection<JClass> getDirectSubclassesOf(JClass jclass);

    boolean isSubclass(JClass superclass, JClass subclass);

    /**
     * Returns all subclasses of the given class (including itself).
     * If the given class is an interface, then return all its
     * direct/indirect subinterfaces and their all direct/indirect implementors;
     * otherwise, return all its direct/indirect subclasses.
     *
     * @param jclass the given class.
     * @return all subclasses of given class (including itself).
     */
    Collection<JClass> getAllSubclassesOf(JClass jclass);

    /**
     * @return the direct inner classes of {@code jclass}. If {@code jclass}
     * does not have inner classes, an empty collection is returned.
     */
    Collection<JClass> getDirectInnerClassesOf(JClass jclass);
}
