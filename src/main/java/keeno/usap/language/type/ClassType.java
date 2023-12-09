

package keeno.usap.language.type;

import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JClassLoader;
import keeno.usap.util.Hashes;

public class ClassType implements ReferenceType {

    private final JClassLoader loader;

    private final String name;

    /**
     * The cache of {@link ClassType#getJClass()}.
     */
    private transient JClass jclass;

    public ClassType(JClassLoader loader, String name) {
        this.loader = loader;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public JClass getJClass() {
        if (jclass == null) {
            jclass = loader.loadClass(name);
        }
        return jclass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassType classType = (ClassType) o;
        return loader.equals(classType.loader)
                && name.equals(classType.name);
    }

    @Override
    public int hashCode() {
        return Hashes.hash(loader, name);
    }

    @Override
    public String toString() {
        return name;
    }
}
