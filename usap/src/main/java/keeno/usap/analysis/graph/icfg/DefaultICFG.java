

package keeno.usap.analysis.graph.icfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.graph.cfg.CFG;
import keeno.usap.analysis.graph.cfg.CFGEdge;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Return;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ClassType;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.collection.Views;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static keeno.usap.analysis.graph.icfg.ICFGBuilder.getCFGOf;

class DefaultICFG extends AbstractICFG<JMethod, Stmt> {

    private static final Logger logger = LogManager.getLogger(DefaultICFG.class);

    private final MultiMap<Stmt, ICFGEdge<Stmt>> inEdges = Maps.newMultiMap();

    private final MultiMap<Stmt, ICFGEdge<Stmt>> outEdges = Maps.newMultiMap();

    private final Map<Stmt, CFG<Stmt>> stmtToCFG = Maps.newLinkedHashMap();

    DefaultICFG(CallGraph<Stmt, JMethod> callGraph) {
        super(callGraph);
        build(callGraph);
    }

    private void build(CallGraph<Stmt, JMethod> callGraph) {
        callGraph.forEach(method -> {
            CFG<Stmt> cfg = getCFGOf(method);
            if (cfg == null) {
                logger.warn("CFG of {} is absent, try to fix this" +
                        " by adding option: -scope REACHABLE", method);
                return;
            }
            cfg.forEach(stmt -> {
                stmtToCFG.put(stmt, cfg);
                cfg.getOutEdgesOf(stmt).forEach(edge -> {
                    ICFGEdge<Stmt> local = isCallSite(stmt) ?
                            new CallToReturnEdge<>(edge) :
                            new NormalEdge<>(edge);
                    outEdges.put(stmt, local);
                    inEdges.put(edge.target(), local);
                });
                if (isCallSite(stmt)) {
                    getCalleesOf(stmt).forEach(callee -> {
                        if (getCFGOf(callee) == null) {
                            logger.warn("CFG of {} is missing", callee);
                            return;
                        }
                        // Add call edges
                        Stmt entry = getEntryOf(callee);
                        CallEdge<Stmt> call = new CallEdge<>(stmt, entry, callee);
                        outEdges.put(stmt, call);
                        inEdges.put(entry, call);
                        // Add return edges
                        Stmt exit = getExitOf(callee);
                        Set<Var> retVars = Sets.newHybridSet();
                        Set<ClassType> exceptions = Sets.newHybridSet();
                        // The exit node of CFG is mock, thus it is not
                        // a real return or excepting Stmt. We need to
                        // collect return and exception information from
                        // the real return and excepting Stmts, and attach
                        // them to the ReturnEdge.
                        getCFGOf(callee).getInEdgesOf(exit).forEach(retEdge -> {
                            if (retEdge.getKind() == CFGEdge.Kind.RETURN) {
                                Return ret = (Return) retEdge.source();
                                if (ret.getValue() != null) {
                                    retVars.add(ret.getValue());
                                }
                            }
                            if (retEdge.isExceptional()) {
                                exceptions.addAll(retEdge.getExceptions());
                            }
                        });
                        getReturnSitesOf(stmt).forEach(retSite -> {
                            ReturnEdge<Stmt> ret = new ReturnEdge<>(
                                    exit, retSite, stmt, retVars, exceptions);
                            outEdges.put(exit, ret);
                            inEdges.put(retSite, ret);
                        });
                    });
                }
            });
        });
    }

    @Override
    public Set<ICFGEdge<Stmt>> getInEdgesOf(Stmt stmt) {
        return inEdges.get(stmt);
    }

    @Override
    public Set<ICFGEdge<Stmt>> getOutEdgesOf(Stmt stmt) {
        return outEdges.get(stmt);
    }

    @Override
    public Stmt getEntryOf(JMethod method) {
        return getCFGOf(method).getEntry();
    }

    @Override
    public Stmt getExitOf(JMethod method) {
        return getCFGOf(method).getExit();
    }

    @Override
    public Set<Stmt> getReturnSitesOf(Stmt callSite) {
        assert isCallSite(callSite);
        return stmtToCFG.get(callSite).getSuccsOf(callSite);
    }

    @Override
    public JMethod getContainingMethodOf(Stmt stmt) {
        return stmtToCFG.get(stmt).getMethod();
    }

    @Override
    public boolean isCallSite(Stmt stmt) {
        return stmt instanceof Invoke;
    }

    @Override
    public boolean hasEdge(Stmt source, Stmt target) {
        return getOutEdgesOf(source)
                .stream()
                .anyMatch(edge -> edge.target().equals(target));
    }

    @Override
    public Set<Stmt> getPredsOf(Stmt stmt) {
        return Views.toMappedSet(getInEdgesOf(stmt), ICFGEdge::source);
    }

    @Override
    public Set<Stmt> getSuccsOf(Stmt stmt) {
        return Views.toMappedSet(getOutEdgesOf(stmt), ICFGEdge::target);
    }

    @Override
    public Set<Stmt> getNodes() {
        return Collections.unmodifiableSet(stmtToCFG.keySet());
    }
}
