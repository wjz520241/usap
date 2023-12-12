

package keeno.usap;

import keeno.usap.config.Options;
import keeno.usap.frontend.cache.CachedIRBuilder;
import keeno.usap.ir.IRBuilder;
import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.natives.NativeModel;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.util.AbstractResultHolder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 管理正在分析的程序的整个程序信息。请注意，这个类的setter是受保护的：它们应该由WorldBuilder调用（一次），而不是分析类。
 */
public final class World extends AbstractResultHolder
        implements Serializable {

    /**
     * ZA WARUDO, i.e., the current world.
     */
    private static World theWorld;

    /**
     * The callbacks that will be invoked at resetting.
     * This is useful to clear class-level caches.
     */
    private static final List<Runnable> resetCallbacks = new ArrayList<>();

    /**
     * Notes: This field is {@code transient} because it
     * should be set after deserialization.
     */
    private transient Options options;

    private TypeSystem typeSystem;

    private ClassHierarchy classHierarchy;

    /**
     * Notes: add {@code transient} to wrap this {@link IRBuilder} using
     * {@link keeno.usap.frontend.cache.CachedIRBuilder} in serialization.
     *
     * @see #writeObject(ObjectOutputStream)
     * @see #readObject(ObjectInputStream)
     */
    private transient IRBuilder irBuilder;

    private NativeModel nativeModel;

    private JMethod mainMethod;

    private Collection<JMethod> implicitEntries;

    /**
     * Sets current world to {@code world}.
     */
    public static void set(World world) {
        theWorld = world;
    }

    /**
     * @return the current {@code World} instance.
     */
    public static World get() {
        return theWorld;
    }

    public static void registerResetCallback(Runnable callback) {
        resetCallbacks.add(callback);
    }

    public static void reset() {
        theWorld = null;
        resetCallbacks.forEach(Runnable::run);
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        if (this.options != null) {
            throw new IllegalStateException("Options already set");
        }
        this.options = options;
    }

    public TypeSystem getTypeSystem() {
        return typeSystem;
    }

    public void setTypeSystem(TypeSystem typeSystem) {
        if (this.typeSystem != null) {
            throw new IllegalStateException("TypeSystem already set");
        }
        this.typeSystem = typeSystem;
    }

    public ClassHierarchy getClassHierarchy() {
        return classHierarchy;
    }

    public void setClassHierarchy(ClassHierarchy classHierarchy) {
        if (this.classHierarchy != null) {
            throw new IllegalStateException("ClassHierarchy already set");
        }
        this.classHierarchy = classHierarchy;
    }

    public IRBuilder getIRBuilder() {
        return irBuilder;
    }

    public void setIRBuilder(IRBuilder irBuilder) {
        this.irBuilder = irBuilder;
    }

    public NativeModel getNativeModel() {
        return nativeModel;
    }

    public void setNativeModel(NativeModel nativeModel) {
        this.nativeModel = nativeModel;
    }

    public JMethod getMainMethod() {
        return mainMethod;
    }

    public void setMainMethod(JMethod mainMethod) {
        if (this.mainMethod != null) {
            throw new IllegalStateException("Main method already set");
        }
        this.mainMethod = mainMethod;
    }

    public Collection<JMethod> getImplicitEntries() {
        return implicitEntries;
    }

    public void setImplicitEntries(Collection<JMethod> implicitEntries) {
        this.implicitEntries = implicitEntries;
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(new CachedIRBuilder(irBuilder, classHierarchy));
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        irBuilder = (IRBuilder) s.readObject();
    }
}
