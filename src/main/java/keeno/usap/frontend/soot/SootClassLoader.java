

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
                // 新类必须首先放入类映射中，在构建时（jclass）也可能触发加载新创建的类(如加载父类时)。不将类放入类中可能会导致无限递归。
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
