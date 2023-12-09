

package keeno.usap.language.classes;

import java.io.Serializable;
import java.util.Collection;

public interface JClassLoader extends Serializable {

    JClass loadClass(String name);

    Collection<JClass> getLoadedClasses();
}
