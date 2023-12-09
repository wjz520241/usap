

package keeno.usap.frontend.soot;

import keeno.usap.language.classes.Modifier;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static keeno.usap.util.collection.Maps.newConcurrentMap;

class Modifiers {

    private static final ConcurrentMap<Integer, Set<Modifier>> modMap
            = newConcurrentMap();

    private Modifiers() {
    }

    static Set<Modifier> convert(int modifiers) {
        return modMap.computeIfAbsent(modifiers, m -> {
            Set<Modifier> result = EnumSet.noneOf(Modifier.class);
            if (soot.Modifier.isAbstract(m)) {
                result.add(Modifier.ABSTRACT);
            }
            if (soot.Modifier.isFinal(m)) {
                result.add(Modifier.FINAL);
            }
            if (soot.Modifier.isInterface(m)) {
                result.add(Modifier.INTERFACE);
            }
            if (soot.Modifier.isNative(m)) {
                result.add(Modifier.NATIVE);
            }
            if (soot.Modifier.isPrivate(m)) {
                result.add(Modifier.PRIVATE);
            }
            if (soot.Modifier.isProtected(m)) {
                result.add(Modifier.PROTECTED);
            }
            if (soot.Modifier.isPublic(m)) {
                result.add(Modifier.PUBLIC);
            }
            if (soot.Modifier.isStatic(m)) {
                result.add(Modifier.STATIC);
            }
            if (soot.Modifier.isSynchronized(m)) {
                result.add(Modifier.SYNCHRONIZED);
            }
            if (soot.Modifier.isTransient(m)) {
                result.add(Modifier.TRANSIENT);
            }
            if (soot.Modifier.isVolatile(m)) {
                result.add(Modifier.VOLATILE);
            }
            if (soot.Modifier.isStrictFP(m)) {
                result.add(Modifier.STRICTFP);
            }
            if (soot.Modifier.isAnnotation(m)) {
                result.add(Modifier.ANNOTATION);
            }
            if (soot.Modifier.isEnum(m)) {
                result.add(Modifier.ENUM);
            }
            if (soot.Modifier.isSynthetic(m)) {
                result.add(Modifier.SYNTHETIC);
            }
            return Collections.unmodifiableSet(result);
        });
    }
}
