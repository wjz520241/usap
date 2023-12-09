

package keeno.usap.analysis.sideeffect;

import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.ir.exp.FieldAccess;
import keeno.usap.ir.exp.InstanceFieldAccess;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.StoreArray;
import keeno.usap.ir.stmt.StoreField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.Indexer;
import keeno.usap.util.collection.CollectionUtils;
import keeno.usap.util.collection.IndexerBitSet;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.graph.MergedNode;
import keeno.usap.util.graph.MergedSCCGraph;
import keeno.usap.util.graph.TopologicalSorter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes modification information based on pointer analysis
 * and topological sorting of call graph.
 */
class TopologicalSolver {

    private final boolean onlyApp;

    TopologicalSolver(boolean onlyApp) {
        this.onlyApp = onlyApp;
    }

    SideEffect solve(PointerAnalysisResult pta) {
        CallGraph<Invoke, JMethod> callGraph = pta.getCallGraph();
        // 1. compute the objects directly modified by each method and stmt
        Map<JMethod, Set<Obj>> methodDirectMods = Maps.newMap();
        Map<Stmt, Set<Obj>> stmtDirectMods = Maps.newMap();
        computeDirectMods(pta, callGraph, stmtDirectMods, methodDirectMods);
        // 2. compute the objects directly modified by
        //    the methods of each SCC in the call graph
        var mg = new MergedSCCGraph<>(callGraph);
        Map<JMethod, Set<Obj>> sccDirectMods = computeSCCDirectMods(
                mg.getNodes(), methodDirectMods);
        // 3. fully compute the objects modified by each method
        Indexer<Obj> indexer = pta.getObjectIndexer();
        Map<JMethod, Set<Obj>> methodMods = computeMethodMods(
                mg, callGraph, sccDirectMods, indexer);
        return new SideEffect(methodMods, stmtDirectMods, callGraph);
    }

    private void computeDirectMods(
            PointerAnalysisResult pta,
            CallGraph<?, JMethod> callGraph,
            Map<Stmt, Set<Obj>> stmtDirectMods,
            Map<JMethod, Set<Obj>> methodDirectMods) {
        callGraph.forEach(method -> {
            Set<Obj> mMods = Sets.newHybridSet();
            method.getIR().forEach(stmt -> {
                Set<Obj> sMods = Set.of();
                if (stmt instanceof StoreField storeField) {
                    FieldAccess fieldAccess = storeField.getFieldAccess();
                    if (fieldAccess instanceof InstanceFieldAccess instAccess) {
                        Var base = instAccess.getBase();
                        sMods = pta.getPointsToSet(base);
                    }
                } else if (stmt instanceof StoreArray storeArray) {
                    Var base = storeArray.getArrayAccess().getBase();
                    sMods = pta.getPointsToSet(base);
                }
                if (!sMods.isEmpty()) {
                    sMods = sMods.stream()
                            .filter(this::isRelevant)
                            .collect(Collectors.toUnmodifiableSet());
                }
                if (!sMods.isEmpty()) {
                    mMods.addAll(sMods);
                    stmtDirectMods.put(stmt, sMods);
                }
            });
            if (!mMods.isEmpty()) {
                methodDirectMods.put(method, mMods);
            }
        });
    }

    private boolean isRelevant(Obj obj) {
        if (onlyApp && obj.getContainerMethod().isPresent()) {
            return obj.getContainerMethod().get().isApplication();
        }
        return false;
    }

    private static Map<JMethod, Set<Obj>> computeSCCDirectMods(
            Set<MergedNode<JMethod>> sccs,
            Map<JMethod, Set<Obj>> methodDirectMods) {
        Map<JMethod, Set<Obj>> sccDirectMods = Maps.newMap();
        sccs.forEach(scc -> {
            Set<Obj> mods = Sets.newHybridSet();
            scc.getNodes().forEach(m ->
                    mods.addAll(methodDirectMods.getOrDefault(m, Set.of())));
            scc.getNodes().forEach(m -> sccDirectMods.put(m, mods));
        });
        return sccDirectMods;
    }

    private static Map<JMethod, Set<Obj>> computeMethodMods(
            MergedSCCGraph<JMethod> mg,
            CallGraph<?, JMethod> callGraph,
            Map<JMethod, Set<Obj>> sccDirectMods,
            Indexer<Obj> indexer) {
        Map<JMethod, Set<Obj>> methodMods = Maps.newMap();
        // to accelerate side-effect analysis, we propagate modified objects
        // of methods (methodMods) based on topological sorting of call graph,
        // so that each method only needs to be processed once
        var sorter = new TopologicalSorter<>(mg, true);
        sorter.get().forEach(scc -> {
            Set<Obj> mods = new IndexerBitSet<>(indexer, true);
            // add SCC direct mods
            Set<JMethod> sccNodes = Sets.newSet(scc.getNodes());
            JMethod rep = CollectionUtils.getOne(sccNodes);
            mods.addAll(sccDirectMods.get(rep));
            // add callees' mods
            sccNodes.forEach(m -> {
                callGraph.getCalleesOfM(m)
                        .stream()
                        // avoid redundantly adding SCC direct mods
                        .filter(callee -> !sccNodes.contains(callee))
                        .forEach(callee -> mods.addAll(
                                methodMods.getOrDefault(callee, Set.of())));
            });
            if (!mods.isEmpty()) {
                sccNodes.forEach(m -> methodMods.put(m, mods));
            }
        });
        return methodMods;
    }
}
