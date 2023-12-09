

package keeno.usap.analysis.pta.toolkit.mahjong;

import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Sets;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

class DFA {

    private static final DFAState DEAD_STATE = new DFAState(Set.of(), Set.of());

    private final DFAState q0;

    private Set<DFAState> states, allStates;

    DFA(DFAState q0) {
        this.q0 = q0;
    }

    /**
     * @return Set of states (excluding dead state).
     */
    Set<DFAState> getStates() {
        if (states == null) {
            computeStates();
        }
        return states;
    }

    /**
     * @return Set of all states (including dead state).
     */
    Set<DFAState> getAllStates() {
        if (allStates == null) {
            computeStates();
        }
        return allStates;
    }

    private void computeStates() {
        Queue<DFAState> queue = new ArrayDeque<>();
        queue.add(q0);
        states = Sets.newSet();
        while (!queue.isEmpty()) {
            DFAState s = queue.poll();
            if (!states.contains(s)) {
                states.add(s);
                queue.addAll(s.getNextMap().values());
            }
        }
        allStates = Sets.newSet(states);
        allStates.add(DEAD_STATE);
    }

    DFAState getStartState() {
        return q0;
    }

    DFAState getDeadState() {
        return DEAD_STATE;
    }

    boolean isDeadState(DFAState s) {
        return DEAD_STATE == s;
    }

    DFAState nextState(DFAState s, Field f) {
        if (isDeadState(s)) {
            return getDeadState();
        }
        Map<Field, DFAState> nextMap = s.getNextMap();
        if (nextMap.containsKey(f)) {
            return nextMap.get(f);
        }
        return getDeadState();
    }

    Set<Type> outputOf(DFAState s) {
        return s.getOutput();
    }

    Set<Field> outEdgesOf(DFAState s) {
        Map<Field, DFAState> nextMap = s.getNextMap();
        return nextMap.keySet();
    }

    boolean containsState(DFAState s) {
        return getAllStates().contains(s);
    }
}
