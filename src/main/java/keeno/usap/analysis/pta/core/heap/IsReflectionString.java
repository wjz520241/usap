

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.World;
import keeno.usap.language.classes.ClassMember;
import keeno.usap.language.classes.StringReps;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.collection.Streams;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Predicate for checking whether given string constants are
 * reflection-relevant, i.e., class names, method names, or field names.
 */
public class IsReflectionString implements Predicate<String> {

    private final Set<String> reflectionStrings = Sets.newSet(4096);

    public IsReflectionString() {
        World.get().getClassHierarchy().allClasses().forEach(c -> {
            reflectionStrings.add(c.getName());
            Streams.concat(c.getDeclaredMethods().stream(),
                            c.getDeclaredFields().stream())
                    .map(ClassMember::getName)
                    .forEach(reflectionStrings::add);
        });
    }

    @Override
    public boolean test(String s) {
        return (StringReps.isJavaClassName(s) || StringReps.isJavaIdentifier(s))
                && reflectionStrings.contains(s);
    }
}
