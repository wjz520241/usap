

package keeno.usap.util.graph;

import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import static java.util.function.Predicate.not;

/**
 * Computes reachability information for graph.
 *
 * @param <N> type of graph nodes.
 */
public class Reachability<N> {

    private final Graph<N> graph;

    /**
     * Maps a source node to all nodes reachable from it on the graph.
     */
    private final MultiMap<N, N> source2Reachable = Maps.newMultiMap();

    /**
     * Maps a target node to all nodes that can reach it on the graph.
     */
    private final MultiMap<N, N> target2CanReach = Maps.newMultiMap();

    public Reachability(Graph<N> graph) {
        this.graph = graph;
    }

    /**
     * @return all nodes those can be reached from {@code source}.
     */
    public Set<N> reachableNodesFrom(N source) {
        if (!source2Reachable.containsKey(source)) {
            Set<N> visited = Sets.newSet();
            Deque<N> stack = new ArrayDeque<>();
            stack.push(source);
            while (!stack.isEmpty()) {
                N node = stack.pop();
                if (visited.add(node)) {
                    graph.getSuccsOf(node)
                            .stream()
                            .filter(not(visited::contains))
                            .forEach(stack::push);
                }
            }
            source2Reachable.putAll(source, visited);
        }
        return source2Reachable.get(source);
    }

    /**
     * @return all nodes those can reach {@code target}.
     */
    public Set<N> nodesCanReach(N target) {
        if (!target2CanReach.containsKey(target)) {
            Set<N> visited = Sets.newSet();
            Deque<N> stack = new ArrayDeque<>();
            stack.push(target);
            while (!stack.isEmpty()) {
                N node = stack.pop();
                if (visited.add(node)) {
                    graph.getPredsOf(node)
                            .stream()
                            .filter(not(visited::contains))
                            .forEach(stack::push);
                }
            }
            target2CanReach.putAll(target, visited);
        }
        return target2CanReach.get(target);
    }
}
