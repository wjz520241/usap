

package keeno.usap.util;

import java.util.Objects;

/**
 * Static utility methods for computing hash code.
 * Avoids array creation of Objects.hash().
 *
 * 在hash计算中通常使用质数作为乘数，31是一个比较好的选择，原因如下：
 * 1.31是一个不大不小的质数，它既不会导致溢出也不会使结果太小。
 * 2.31可以被JVM优化为31 * i，因为31在二进制中只有五个1，这样的话编译器可以用移位和减法运算来代替乘法，从而提高计算效率。
 * 此外，31还有一个比较好的性质：31可以写成2^5-1的形式，即31可以表示为二进制数11111，这样31乘以一个数时就相当于将这个数向左移5位，
 * 然后减去原来的数。这种操作可以使得计算结果更加分散，有助于减少哈希冲突的概率。
 */
public final class Hashes {

    private Hashes() {
    }

    /**
     * @return hash code of two objects.
     * @throws NullPointerException if any parameter is null
     */
    public static int hash(Object o1, Object o2) {
        return o1.hashCode() * 31 + o2.hashCode();
    }

    /**
     * @return hash code of two objects, with null check.
     */
    public static int safeHash(Object o1, Object o2) {
        return Objects.hashCode(o1) * 31 + Objects.hashCode(o2);
    }

    /**
     * @return hash code of three objects.
     * @throws NullPointerException if any parameter is null
     */
    public static int hash(Object o1, Object o2, Object o3) {
        int result = o1.hashCode();
        result = 31 * result + o2.hashCode();
        result = 31 * result + o3.hashCode();
        return result;
    }

    /**
     * @return hash code of four objects, with null check.
     */
    public static int safeHash(Object o1, Object o2, Object o3) {
        int result = Objects.hashCode(o1);
        result = 31 * result + Objects.hashCode(o2);
        result = 31 * result + Objects.hashCode(o3);
        return result;
    }

    /**
     * @return hash code of four objects.
     * @throws NullPointerException if any parameter is null
     */
    public static int hash(Object o1, Object o2, Object o3, Object o4) {
        int result = o1.hashCode();
        result = 31 * result + o2.hashCode();
        result = 31 * result + o3.hashCode();
        result = 31 * result + o4.hashCode();
        return result;
    }

    /**
     * @return hash code of four objects, with null check.
     */
    public static int safeHash(Object o1, Object o2, Object o3, Object o4) {
        int result = Objects.hashCode(o1);
        result = 31 * result + Objects.hashCode(o2);
        result = 31 * result + Objects.hashCode(o3);
        result = 31 * result + Objects.hashCode(o4);
        return result;
    }
}
