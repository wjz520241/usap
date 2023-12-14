

package keeno.usap.util;

import java.util.function.Predicate;

public final class Predicates {

    private Predicates() {
    }

    private enum PresetPredicates implements Predicate<Object> {

        ALWAYS_TRUE {
            @Override
            public boolean test(Object o) {
                return true;
            }

            @Override
            public String toString() {
                return "Predicates.alwaysTrue()";
            }
        },

        ALWAYS_FALSE {
            @Override
            public boolean test(Object o) {
                return false;
            }

            @Override
            public String toString() {
                return "Predicates.alwaysFalse()";
            }
        };

        //withNarrowedType方法的作用是返回一个经过类型缩小的Predicate实例，以便在需要特定类型的Predicate时使用
        // safe contravariant cast
        @SuppressWarnings("unchecked")
        private <T> Predicate<T> withNarrowedType() {
            return (Predicate<T>) this;
        }
    }

    /**
     * @return a predicate that always evaluates to {@code true}.
     */
    public static <T> Predicate<T> alwaysTrue() {
        return PresetPredicates.ALWAYS_TRUE.withNarrowedType();
    }

    /**
     * @return 一个总是计算为false的谓词
     */
    public static <T> Predicate<T> alwaysFalse() {
        return PresetPredicates.ALWAYS_FALSE.withNarrowedType();
    }
}
