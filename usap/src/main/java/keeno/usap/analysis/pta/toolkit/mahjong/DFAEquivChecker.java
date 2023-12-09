

package keeno.usap.analysis.pta.toolkit.mahjong;

import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Pair;
import keeno.usap.util.collection.UnionFindSet;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DFAEquivChecker {

    /**
     * Checks the equivalence of input automata by Hopcroft-Karp algorithm
     * with minor modifications.
     *
     * @return {@code true} if dfa1 and dfa2 are equivalent.
     */
    boolean isEquivalent(DFA dfa1, DFA dfa2) {
        CombinedDFA dfa = new CombinedDFA(dfa1, dfa2);
        Set<DFAState> combinedStates = dfa.getStates();
        UnionFindSet<DFAState> uf = new UnionFindSet<>(combinedStates);
        Deque<Pair<DFAState, DFAState>> stack = new ArrayDeque<>();

        DFAState s1 = dfa1.getStartState();
        DFAState s2 = dfa2.getStartState();
        uf.union(s1, s2);
        stack.push(new Pair<>(s1, s2));
        while (!stack.isEmpty()) {
            Pair<DFAState, DFAState> pair = stack.pop();
            DFAState q1 = pair.first();
            DFAState q2 = pair.second();
            Stream.concat(dfa.outEdgesOf(q1).stream(),
                            dfa.outEdgesOf(q2).stream())
                    .forEach(field -> {
                        DFAState r1 = uf.findRoot(dfa.nextState(q1, field));
                        DFAState r2 = uf.findRoot(dfa.nextState(q2, field));
                        if (r1 != r2) {
                            uf.union(r1, r2);
                            stack.push(new Pair<>(r1, r2));
                        }
                    });
        }
        Collection<Set<DFAState>> mergedStateSets = uf.getDisjointSets();
        return validate(dfa, mergedStateSets);
    }

    /**
     * @return {@code true} if every state set contains no different output
     * (i.e., types)
     */
    private boolean validate(CombinedDFA dfa,
                             Collection<Set<DFAState>> mergedStateSets) {
        for (Set<DFAState> set : mergedStateSets) {
            int minSize = set.stream()
                    .mapToInt(s -> dfa.outputOf(s).size())
                    .min()
                    .orElse(0);
            long unionSize = set.stream()
                    .flatMap(s -> dfa.outputOf(s).stream())
                    .distinct()
                    .count();
            if (unionSize > minSize) {
                return false;
            }
        }
        return true;
    }


    private static class CombinedDFA {

        final DFA dfa1;
        final DFA dfa2;

        private CombinedDFA(DFA dfa1, DFA dfa2) {
            this.dfa1 = dfa1;
            this.dfa2 = dfa2;
        }

        private Set<DFAState> getStates() {
            return Stream
                    .concat(dfa1.getAllStates().stream(),
                            dfa2.getAllStates().stream())
                    .collect(Collectors.toSet());
        }

        private DFAState nextState(DFAState s, Field f) {
            return dfa1.containsState(s) ?
                    dfa1.nextState(s, f) :
                    dfa2.nextState(s, f);
        }

        private Set<Field> outEdgesOf(DFAState s) {
            return dfa1.containsState(s) ?
                    dfa1.outEdgesOf(s) :
                    dfa2.outEdgesOf(s);
        }

        private Set<Type> outputOf(DFAState s) {
            return dfa1.containsState(s) ?
                    dfa1.outputOf(s) :
                    dfa2.outputOf(s);
        }
    }
}
