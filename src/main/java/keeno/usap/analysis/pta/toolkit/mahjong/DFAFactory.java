

package keeno.usap.analysis.pta.toolkit.mahjong;

import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Sets;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

class DFAFactory {

    private final FieldPointsToGraph fpg;

    private Set<DFAState> states, visited;

    private ConcurrentMap<Set<Obj>, DFAState> stateMap;

    DFAFactory(FieldPointsToGraph fpg) {
        this.fpg = fpg;
        buildAllDFA();
    }

    private void buildAllDFA() {
        stateMap = Maps.newConcurrentMap();
        states = Sets.newConcurrentSet();
        visited = Sets.newConcurrentSet();
        fpg.getObjects().parallelStream().forEach(this::buildDFA);
    }

    /**
     * Perform subset construction algorithm to convert an NFA
     * to a DFA. If a set of NFA states are merged to an existing
     * DFA state, then reused the existing DFA state instead of creating
     * an equivalent new one.
     *
     * @param obj the start state (object) of the DFA
     */
    private void buildDFA(Obj obj) {
        Set<Obj> q0Set = Set.of(obj);
        if (!stateMap.containsKey(q0Set)) {
            NFA nfa = new NFA(obj, fpg);
            DFAState startState = getDFAState(q0Set, nfa);
            Queue<DFAState> workList = new ArrayDeque<>();
            states.add(startState);
            workList.add(startState);
            while (!workList.isEmpty()) {
                DFAState s = workList.poll();
                if (!visited.contains(s)) {
                    visited.add(s);
                    Set<Field> fields = fields(nfa, s.getObjects());
                    fields.forEach(f -> {
                        Set<Obj> nextNFAStates = move(nfa, s.getObjects(), f);
                        DFAState nextState = getDFAState(nextNFAStates, nfa);
                        if (!states.contains(nextState)) {
                            states.add(nextState);
                            workList.add(nextState);
                        }
                        addTransition(s, f, nextState);
                    });
                }
            }
        }
    }

    private DFAState getDFAState(Set<Obj> objs, NFA nfa) {
        return stateMap.computeIfAbsent(objs, objects -> {
            Set<Type> output = objects.stream()
                    .map(nfa::outputOf)
                    .collect(Collectors.toSet());
            return new DFAState(objects, output);
        });
    }

    private Set<Obj> move(NFA nfa, Set<Obj> objs, Field f) {
        return objs.stream()
                .map(obj -> nfa.nextStates(obj, f))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<Field> fields(NFA nfa, Set<Obj> objs) {
        return objs.stream()
                .map(nfa::outEdgesOf)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private void addTransition(DFAState s, Field f, DFAState nextState) {
        s.addTransition(f, nextState);
    }

    DFA getDFA(Obj obj) {
        DFAState q0 = stateMap.get(Set.of(obj));
        return new DFA(q0);
    }
}
