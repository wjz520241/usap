

package keeno.usap.frontend.soot;

import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JClassLoader;
import keeno.usap.util.collection.Maps;
import soot.Scene;
import soot.SootClass;

import java.util.Collection;
import java.util.Map;

class SootClassLoader implements JClassLoader {

    private final transient Scene scene;

    private final ClassHierarchy hierarchy;

    private final boolean allowPhantom;

    private transient Converter converter;

    private final Map<String, JClass> classes = Maps.newMap(1024);

    SootClassLoader(Scene scene, ClassHierarchy hierarchy, boolean allowPhantom) {
        this.scene = scene;
        this.hierarchy = hierarchy;
        this.allowPhantom = allowPhantom;
    }

    @Override
    public JClass loadClass(String name) {
        JClass jclass = classes.get(name);
        if (jclass == null && scene != null) {
            SootClass sootClass = scene.getSootClassUnsafe(name, false);
            if (sootClass != null && (!sootClass.isPhantom() || allowPhantom)) {
                // TODO: handle phantom class more comprehensively
                jclass = new JClass(this, sootClass.getName(),
                        sootClass.moduleName);
                // New class must be put into classes map at first,
                // at build(jclass) may also trigger the loading of
                // the new created class. Not putting the class into classes
                // may cause infinite recursion.
                classes.put(name, jclass);
                new SootClassBuilder(converter, sootClass).build(jclass);
                hierarchy.addClass(jclass);
            }
        }
        // TODO: add warning for missing classes
        return jclass;
    }

    @Override
    public Collection<JClass> getLoadedClasses() {
        return classes.values();
    }

    void setConverter(Converter converter) {
        this.converter = converter;
    }
}
