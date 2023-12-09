

package keeno.usap.analysis.pta.toolkit.mahjong;

import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Maps;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

class DFAState {

    private final Set<Obj> objs;

    private final Set<Type> output;

    private final ConcurrentMap<Field, DFAState> nextMap;

    private int hashCode = 0;

    DFAState(Set<Obj> objs, Set<Type> output) {
        this.objs = objs;
        this.output = output;
        this.nextMap = Maps.newConcurrentMap();
    }

    Set<Obj> getObjects() {
        return objs;
    }

    Set<Type> getOutput() {
        return output;
    }

    void addTransition(Field f, DFAState nextState) {
        nextMap.put(f, nextState);
    }

    Map<Field, DFAState> getNextMap() {
        return nextMap;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = objs.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DFAState anoDFAState)) {
            return false;
        }
        return getObjects().equals(anoDFAState.getObjects());
    }

    @Override
    public String toString() {
        return getObjects().stream()
                .map(Objects::toString)
                .collect(Collectors.toSet())
                .toString();
    }
}
