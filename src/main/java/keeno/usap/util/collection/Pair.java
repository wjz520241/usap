

package keeno.usap.util.collection;

import java.io.Serializable;

public record Pair<T1, T2>(T1 first, T2 second)
        implements Serializable {

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }
}
