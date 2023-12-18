

package keeno.usap.analysis.pta.core.solver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.analysis.graph.callgraph.CallGraphs;
import keeno.usap.analysis.graph.callgraph.CallKind;
import keeno.usap.analysis.graph.callgraph.Edge;
import keeno.usap.analysis.graph.flowgraph.FlowKind;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.PointerAnalysisResultImpl;
import keeno.usap.analysis.pta.core.cs.CSCallGraph;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.ArrayIndex;
import keeno.usap.analysis.pta.core.cs.element.CSCallSite;
import keeno.usap.analysis.pta.core.cs.element.CSManager;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.cs.element.InstanceField;
import keeno.usap.analysis.pta.core.cs.element.Pointer;
import keeno.usap.analysis.pta.core.cs.element.StaticField;
import keeno.usap.analysis.pta.core.cs.selector.ContextSelector;
import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.HeapModel;
import keeno.usap.analysis.pta.core.heap.MockObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.plugin.Plugin;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.analysis.pta.pts.PointsToSetFactory;
import keeno.usap.config.AnalysisOptions;
import keeno.usap.ir.IR;
import keeno.usap.ir.exp.CastExp;
import keeno.usap.ir.exp.InvokeExp;
import keeno.usap.ir.exp.InvokeStatic;
import keeno.usap.ir.exp.Literal;
import keeno.usap.ir.exp.NewExp;
import keeno.usap.ir.exp.NewMultiArray;
import keeno.usap.ir.exp.ReferenceLiteral;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.ir.stmt.AssignLiteral;
import keeno.usap.ir.stmt.Cast;
import keeno.usap.ir.stmt.Copy;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.LoadArray;
import keeno.usap.ir.stmt.LoadField;
import keeno.usap.ir.stmt.New;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.StmtVisitor;
import keeno.usap.ir.stmt.StoreArray;
import keeno.usap.ir.stmt.StoreField;
import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static keeno.usap.language.classes.Signatures.FINALIZE;
import static keeno.usap.language.classes.Signatures.FINALIZER_REGISTER;

public class DefaultSolver implements Solver {

    private static final Logger logger = LogManager.getLogger(DefaultSolver.class);

    /**
     * Descriptor for array objects created implicitly by multiarray instruction.
     */
    private static final Descriptor MULTI_ARRAY_DESC = () -> "MultiArrayObj";

    /**
     * Number that represents unlimited elapsed time.
     */
    private static final long UNLIMITED = -1;

    private final AnalysisOptions options;

    private final HeapModel heapModel;

    private final ContextSelector contextSelector;

    private final CSManager csManager;

    private final ClassHierarchy hierarchy;

    private final TypeSystem typeSystem;

    private final PointsToSetFactory ptsFactory;

    private final PropagateTypes propTypes;

    /**
     * Whether only analyzes application code.
     */
    private final boolean onlyApp;

    /**
     * Time limit for pointer analysis (in seconds).
     */
    private final long timeLimit;

    private TimeLimiter timeLimiter;

    /**
     * Whether the analysis has reached time limit.
     */
    private volatile boolean isTimeout;

    private Plugin plugin;

    private WorkList workList;

    private CSCallGraph callGraph;

    private PointerFlowGraph pointerFlowGraph;

    private Set<JMethod> reachableMethods;

    /**
     * Set of classes that have been initialized.
     */
    private Set<JClass> initializedClasses;

    /**
     * Set of methods to be intercepted and ignored.
     */
    private Set<JMethod> ignoredMethods;

    private StmtProcessor stmtProcessor;

    private PointerAnalysisResult result;

    @SuppressWarnings("unchecked")
    public DefaultSolver(AnalysisOptions options, HeapModel heapModel,
                         ContextSelector contextSelector, CSManager csManager) {
        this.options = options;
        this.heapModel = heapModel;
        this.contextSelector = contextSelector;
        this.csManager = csManager;
        hierarchy = World.get().getClassHierarchy();
        typeSystem = World.get().getTypeSystem();
        ptsFactory = new PointsToSetFactory(csManager.getObjectIndexer());
        propTypes = new PropagateTypes((List<String>) options.get("propagate-types"));
        onlyApp = options.getBoolean("only-app");
        timeLimit = options.getInt("time-limit");
    }

    @Override
    public AnalysisOptions getOptions() {
        return options;
    }

    @Override
    public HeapModel getHeapModel() {
        return heapModel;
    }

    @Override
    public ContextSelector getContextSelector() {
        return contextSelector;
    }

    @Override
    public CSManager getCSManager() {
        return csManager;
    }

    @Override
    public ClassHierarchy getHierarchy() {
        return hierarchy;
    }

    @Override
    public TypeSystem getTypeSystem() {
        return typeSystem;
    }

    @Override
    public CSCallGraph getCallGraph() {
        return callGraph;
    }

    @Override
    public PointsToSet getPointsToSetOf(Pointer pointer) {
        PointsToSet pts = pointer.getPointsToSet();
        if (pts == null) {
            pts = ptsFactory.make();
            pointer.setPointsToSet(pts);
        }
        return pts;
    }

    @Override
    public PointsToSet makePointsToSet() {
        return ptsFactory.make();
    }

    @Override
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    // ---------- solver logic starts ----------

    /**
     * Runs pointer analysis algorithm.
     */
    @Override
    public void solve() {
        initialize();
        analyze();
    }

    /**
     * Initializes pointer analysis.
     */
    private void initialize() {
        callGraph = new CSCallGraph(csManager);
        pointerFlowGraph = new PointerFlowGraph(csManager);
        workList = new WorkList();
        reachableMethods = Sets.newSet();
        initializedClasses = Sets.newSet();
        ignoredMethods = Sets.newSet();
        stmtProcessor = new StmtProcessor();
        isTimeout = false;
        if (timeLimit != UNLIMITED) {
            timeLimiter = new TimeLimiter(timeLimit);
            timeLimiter.countDown();
        }
        plugin.onStart();
    }

    private class TimeLimiter {

        private static final long MILLIS_FACTOR = 1000;

        private final Thread thread;

        /**
         * @param seconds the time limit.
         */
        private TimeLimiter(long seconds) {
            thread = new Thread(() -> {
                try {
                    Thread.sleep(seconds * MILLIS_FACTOR);
                } catch (InterruptedException ignored) {
                }
                isTimeout = true;
            });
        }

        /**
         * Starts count down.
         */
        private void countDown() {
            thread.start();
        }

        /**
         * Stops count down.
         */
        private void stop() {
            thread.interrupt();
        }
    }

    /**
     * Processes work list entries until the work list is empty.
     * 参考《软件分析》第十一、十二节算法讲解
     */
    private void analyze() {
        while (!workList.isEmpty() && !isTimeout) {
            WorkList.Entry entry = workList.pollEntry();
            if (entry instanceof WorkList.PointerEntry pEntry) {
                Pointer p = pEntry.pointer();
                PointsToSet pts = pEntry.pointsToSet();
                PointsToSet diff = propagate(p, pts);
                if (!diff.isEmpty() && p instanceof CSVar v) {
                    processInstanceStore(v, diff);
                    processInstanceLoad(v, diff);
                    processArrayStore(v, diff);
                    processArrayLoad(v, diff);
                    processCall(v, diff);
                    plugin.onNewPointsToSet(v, diff);
                }
            } else if (entry instanceof WorkList.CallEdgeEntry eEntry) {
                processCallEdge(eEntry.edge());
            }
        }
        if (!workList.isEmpty() && isTimeout) {
            logger.warn("Pointer analysis stops early as it reaches time limit ({} seconds)," +
                    " and the result may be unsound!", timeLimit);
        } else if (timeLimiter != null) { // finish normally but time limiter is still running
            timeLimiter.stop();
        }
        plugin.onFinish();
    }

    /**
     * Propagates pointsToSet to pt(pointer) and its PFG successors,
     * returns the difference set of pointsToSet and pt(pointer).
     */
    private PointsToSet propagate(Pointer pointer, PointsToSet pointsToSet) {
        logger.trace("Propagate {} to {}", pointsToSet, pointer);
        Set<Predicate<CSObj>> filters = pointer.getFilters();
        if (!filters.isEmpty()) {
            // apply filters (of the pointer) on pointsToSet
            pointsToSet = pointsToSet.objects()
                    .filter(o -> filters.stream().allMatch(f -> f.test(o)))
                    .collect(ptsFactory::make, PointsToSet::addObject, PointsToSet::addAll);
        }
        PointsToSet diff = getPointsToSetOf(pointer).addAllDiff(pointsToSet);
        if (!diff.isEmpty()) {
            pointerFlowGraph.getOutEdgesOf(pointer).forEach(edge -> {
                Pointer target = edge.target();
                edge.getTransfers().forEach(transfer ->
                        addPointsTo(target, transfer.apply(edge, diff)));
            });
        }
        return diff;
    }

    /**
     * Processes instance stores when points-to set of the base variable changes.
     *
     * @param baseVar the base variable
     * @param pts     set of new discovered objects pointed by the variable.
     */
    private void processInstanceStore(CSVar baseVar, PointsToSet pts) {
        Context context = baseVar.getContext();
        Var var = baseVar.getVar();
        for (StoreField store : var.getStoreFields()) {
            Var fromVar = store.getRValue();
            if (propTypes.isAllowed(fromVar)) {
                CSVar from = csManager.getCSVar(context, fromVar);
                JField field = store.getFieldRef().resolve();
                pts.forEach(baseObj -> {
                    if (baseObj.getObject().isFunctional()) {
                        InstanceField instField = csManager.getInstanceField(baseObj, field);
                        addPFGEdge(from, instField, FlowKind.INSTANCE_STORE);
                    }
                });
            }
        }
    }

    /**
     * Processes instance loads when points-to set of the base variable changes.
     *
     * @param baseVar the base variable
     * @param pts     set of new discovered objects pointed by the variable.
     */
    private void processInstanceLoad(CSVar baseVar, PointsToSet pts) {
        Context context = baseVar.getContext();
        Var var = baseVar.getVar();
        for (LoadField load : var.getLoadFields()) {
            Var toVar = load.getLValue();
            if (propTypes.isAllowed(toVar)) {
                CSVar to = csManager.getCSVar(context, toVar);
                JField field = load.getFieldRef().resolve();
                pts.forEach(baseObj -> {
                    if (baseObj.getObject().isFunctional()) {
                        InstanceField instField = csManager.getInstanceField(baseObj, field);
                        addPFGEdge(instField, to, FlowKind.INSTANCE_LOAD);
                    }
                });
            }
        }
    }

    /**
     * Processes array stores when points-to set of the array variable changes.
     *
     * @param arrayVar the array variable
     * @param pts      set of new discovered arrays pointed by the variable.
     */
    private void processArrayStore(CSVar arrayVar, PointsToSet pts) {
        Context context = arrayVar.getContext();
        Var var = arrayVar.getVar();
        for (StoreArray store : var.getStoreArrays()) {
            Var rvalue = store.getRValue();
            if (propTypes.isAllowed(rvalue)) {
                CSVar from = csManager.getCSVar(context, rvalue);
                pts.forEach(array -> {
                    if (array.getObject().isFunctional()) {
                        ArrayIndex arrayIndex = csManager.getArrayIndex(array);
                        // we need type guard for array stores as Java arrays
                        // are covariant
                        addPFGEdge(from, arrayIndex,
                                FlowKind.ARRAY_STORE, arrayIndex.getType());
                    }
                });
            }
        }
    }

    /**
     * Processes array loads when points-to set of the array variable changes.
     *
     * @param arrayVar the array variable
     * @param pts      set of new discovered arrays pointed by the variable.
     */
    private void processArrayLoad(CSVar arrayVar, PointsToSet pts) {
        Context context = arrayVar.getContext();
        Var var = arrayVar.getVar();
        for (LoadArray load : var.getLoadArrays()) {
            Var lvalue = load.getLValue();
            if (propTypes.isAllowed(lvalue)) {
                CSVar to = csManager.getCSVar(context, lvalue);
                pts.forEach(array -> {
                    if (array.getObject().isFunctional()) {
                        ArrayIndex arrayIndex = csManager.getArrayIndex(array);
                        addPFGEdge(arrayIndex, to, FlowKind.ARRAY_LOAD);
                    }
                });
            }
        }
    }

    /**
     * Processes instance calls when points-to set of the receiver variable changes.
     *
     * @param recv the receiver variable
     * @param pts  set of new discovered objects pointed by the variable.
     */
    private void processCall(CSVar recv, PointsToSet pts) {
        Context context = recv.getContext();
        Var var = recv.getVar();
        for (Invoke callSite : var.getInvokes()) {
            pts.forEach(recvObj -> {
                // resolve callee
                JMethod callee = CallGraphs.resolveCallee(
                        recvObj.getObject().getType(), callSite);
                if (callee != null) {
                    // select context
                    CSCallSite csCallSite = csManager.getCSCallSite(context, callSite);
                    Context calleeContext = contextSelector.selectContext(
                            csCallSite, recvObj, callee);
                    // build call edge
                    CSMethod csCallee = csManager.getCSMethod(calleeContext, callee);
                    addCallEdge(new Edge<>(CallGraphs.getCallKind(callSite),
                            csCallSite, csCallee));
                    // pass receiver object to *this* variable
                    if (!isIgnored(callee)) {
                        addVarPointsTo(calleeContext, callee.getIR().getThis(),
                                recvObj);
                    }
                } else {
                    plugin.onUnresolvedCall(recvObj, context, callSite);
                }
            });
        }
    }

    private void processCallEdge(Edge<CSCallSite, CSMethod> edge) {
        if (callGraph.addEdge(edge)) {
            // process new call edge
            CSMethod csCallee = edge.getCallee();
            addCSMethod(csCallee);
            if (edge.getKind() != CallKind.OTHER
                    && !isIgnored(csCallee.getMethod())) {
                Context callerCtx = edge.getCallSite().getContext();
                Invoke callSite = edge.getCallSite().getCallSite();
                Context calleeCtx = csCallee.getContext();
                JMethod callee = csCallee.getMethod();
                InvokeExp invokeExp = callSite.getInvokeExp();
                // pass arguments to parameters
                for (int i = 0; i < invokeExp.getArgCount(); ++i) {
                    Var arg = invokeExp.getArg(i);
                    if (propTypes.isAllowed(arg)) {
                        Var param = callee.getIR().getParam(i);
                        CSVar argVar = csManager.getCSVar(callerCtx, arg);
                        CSVar paramVar = csManager.getCSVar(calleeCtx, param);
                        addPFGEdge(argVar, paramVar, FlowKind.PARAMETER_PASSING);
                    }
                }
                // pass results to LHS variable
                Var lhs = callSite.getResult();
                if (lhs != null && propTypes.isAllowed(lhs)) {
                    CSVar csLHS = csManager.getCSVar(callerCtx, lhs);
                    for (Var ret : callee.getIR().getReturnVars()) {
                        if (propTypes.isAllowed(ret)) {
                            CSVar csRet = csManager.getCSVar(calleeCtx, ret);
                            addPFGEdge(csRet, csLHS, FlowKind.RETURN);
                        }
                    }
                }
            }
            plugin.onNewCallEdge(edge);
        }
    }

    private boolean isIgnored(JMethod method) {
        return ignoredMethods.contains(method) ||
                onlyApp && !method.isApplication();
    }

    /**
     * Processes new reachable methods.
     */
    private void processNewMethod(JMethod method) {
        if (reachableMethods.add(method)) {
            plugin.onNewMethod(method);
            method.getIR().forEach(stmt -> plugin.onNewStmt(stmt, method));
        }
    }

    private class StmtProcessor {

        /**
         * Information shared by all visitors.
         */
        private final Map<NewMultiArray, Obj[]> newArrays = Maps.newMap();

        private final Map<New, Invoke> registerInvokes = Maps.newMap();

        /**
         * finalize()方法是一个被垃圾回收器（Garbage Collector）调用的方法。当一个对象不再被引用且即将被垃圾回收器回收时，
         * 垃圾回收器会在回收对象之前调用finalize()方法。
         * 这个方法的主要作用是在对象被销毁之前执行一些清理工作或资源释放工作。
         * 例如，可以在finalize()方法中关闭打开的文件、释放占用的系统资源等。需要注意的是，
         * finalize()方法的执行时间是不确定的，因此不适合用于实时性操作。
         * 然而，从Java 9开始，finalize()方法已经被废弃（deprecated），不推荐使用了。原因是该方法的使用存在一些问题，如不可控的执行时间、
         * 无法保证执行与否等。取而代之的是使用其他方式来进行资源的释放和清理，比如使用try-with-resources语句块来自动关闭资源。
         */
        private final JMethod finalize = Objects.requireNonNull(
                hierarchy.getJREMethod(FINALIZE));

        private final MethodRef finalizeRef = finalize.getRef();

        private final MethodRef registerRef = Objects.requireNonNull(
                hierarchy.getJREMethod(FINALIZER_REGISTER)).getRef();

        /**
         * Processes given Stmts in given CSMethod.
         */
        private void process(CSMethod csMethod, Collection<Stmt> stmts) {
            StmtVisitor<Void> visitor = new Visitor(csMethod);
            stmts.forEach(stmt -> stmt.accept(visitor));
        }

        /**
         * Visitor that contains actual processing logics.
         */
        private class Visitor implements StmtVisitor<Void> {

            private final CSMethod csMethod;

            private final Context context;

            private Visitor(CSMethod csMethod) {
                this.csMethod = csMethod;
                this.context = csMethod.getContext();
            }

            @Override
            public Void visit(New stmt) {
                // obtain context-sensitive heap object
                NewExp rvalue = stmt.getRValue();
                Obj obj = heapModel.getObj(stmt);
                Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                addVarPointsTo(context, stmt.getLValue(), heapContext, obj);
                if (rvalue instanceof NewMultiArray) {
                    processNewMultiArray(stmt, heapContext, obj);
                }
                if (hasOverriddenFinalize(rvalue)) {
                    processFinalizer(stmt);
                }
                return null;
            }

            private void processNewMultiArray(
                    New allocSite, Context arrayContext, Obj array) {
                NewMultiArray newMultiArray = (NewMultiArray) allocSite.getRValue();
                Obj[] arrays = newArrays.computeIfAbsent(newMultiArray, nma -> {
                    ArrayType type = nma.getType();
                    Obj[] newArrays = new MockObj[nma.getLengthCount() - 1];
                    for (int i = 1; i < nma.getLengthCount(); ++i) {
                        type = (ArrayType) type.elementType();
                        newArrays[i - 1] = heapModel.getMockObj(MULTI_ARRAY_DESC,
                                allocSite, type, allocSite.getContainer());
                    }
                    return newArrays;
                });
                for (Obj newArray : arrays) {
                    Context elemContext = contextSelector
                            .selectHeapContext(csMethod, newArray);
                    CSObj arrayObj = csManager.getCSObj(arrayContext, array);
                    ArrayIndex arrayIndex = csManager.getArrayIndex(arrayObj);
                    addPointsTo(arrayIndex, elemContext, newArray);
                    array = newArray;
                    arrayContext = elemContext;
                }
            }

            private boolean hasOverriddenFinalize(NewExp newExp) {
                return !finalize.equals(
                        hierarchy.dispatch(newExp.getType(), finalizeRef));
            }

            /**
             * Call Finalizer.register() at allocation sites of objects
             * which override Object.finalize() method.
             * NOTE: finalize() has been deprecated since Java 9, and
             * will eventually be removed.
             */
            private void processFinalizer(New stmt) {
                Invoke registerInvoke = registerInvokes.computeIfAbsent(stmt, s -> {
                    InvokeStatic callSite = new InvokeStatic(registerRef,
                            Collections.singletonList(s.getLValue()));
                    Invoke invoke = new Invoke(csMethod.getMethod(), callSite);
                    invoke.setLineNumber(stmt.getLineNumber());
                    return invoke;
                });
                processInvokeStatic(registerInvoke);
            }

            private void processInvokeStatic(Invoke callSite) {
                JMethod callee = CallGraphs.resolveCallee(null, callSite);
                if (callee != null) {
                    CSCallSite csCallSite = csManager.getCSCallSite(context, callSite);
                    Context calleeCtx = contextSelector.selectContext(csCallSite, callee);
                    CSMethod csCallee = csManager.getCSMethod(calleeCtx, callee);
                    addCallEdge(new Edge<>(CallKind.STATIC, csCallSite, csCallee));
                }
            }

            @Override
            public Void visit(AssignLiteral stmt) {
                Literal literal = stmt.getRValue();
                Type type = literal.getType();
                if (type instanceof ClassType) {
                    // here we only generate objects of ClassType
                    Obj obj = heapModel.getConstantObj((ReferenceLiteral) literal);
                    Context heapContext = contextSelector
                            .selectHeapContext(csMethod, obj);
                    addVarPointsTo(context, stmt.getLValue(), heapContext, obj);
                }
                return null;
            }

            @Override
            public Void visit(Copy stmt) {
                Var rvalue = stmt.getRValue();
                if (propTypes.isAllowed(rvalue)) {
                    CSVar from = csManager.getCSVar(context, rvalue);
                    CSVar to = csManager.getCSVar(context, stmt.getLValue());
                    addPFGEdge(from, to, FlowKind.LOCAL_ASSIGN);
                }
                return null;
            }

            @Override
            public Void visit(Cast stmt) {
                CastExp cast = stmt.getRValue();
                if (propTypes.isAllowed(cast.getValue())) {
                    CSVar from = csManager.getCSVar(context, cast.getValue());
                    CSVar to = csManager.getCSVar(context, stmt.getLValue());
                    addPFGEdge(from, to, FlowKind.CAST, cast.getType());
                }
                return null;
            }

            /**
             * Processes static load.
             */
            @Override
            public Void visit(LoadField stmt) {
                if (stmt.isStatic() && propTypes.isAllowed(stmt.getRValue())) {
                    JField field = stmt.getFieldRef().resolve();
                    StaticField sfield = csManager.getStaticField(field);
                    CSVar to = csManager.getCSVar(context, stmt.getLValue());
                    addPFGEdge(sfield, to, FlowKind.STATIC_LOAD);
                }
                return null;
            }

            /**
             * Processes static store.
             */
            @Override
            public Void visit(StoreField stmt) {
                if (stmt.isStatic() && propTypes.isAllowed(stmt.getRValue())) {
                    JField field = stmt.getFieldRef().resolve();
                    StaticField sfield = csManager.getStaticField(field);
                    CSVar from = csManager.getCSVar(context, stmt.getRValue());
                    addPFGEdge(from, sfield, FlowKind.STATIC_STORE);
                }
                return null;
            }

            /**
             * Processes static invocation.
             */
            @Override
            public Void visit(Invoke stmt) {
                if (stmt.isStatic()) {
                    processInvokeStatic(stmt);
                }
                return null;
            }
        }
    }

    // ---------- solver logic ends ----------

    @Override
    public void addPointsTo(Pointer pointer, PointsToSet pts) {
        workList.addEntry(pointer, pts);
    }

    @Override
    public void addPointsTo(Pointer pointer, CSObj csObj) {
        PointsToSet pts = makePointsToSet();
        pts.addObject(csObj);
        addPointsTo(pointer, pts);
    }

    @Override
    public void addPointsTo(Pointer pointer, Context heapContext, Obj obj) {
        addPointsTo(pointer, csManager.getCSObj(heapContext, obj));
    }

    @Override
    public void addVarPointsTo(Context context, Var var, PointsToSet pts) {
        addPointsTo(csManager.getCSVar(context, var), pts);
    }

    @Override
    public void addVarPointsTo(Context context, Var var, CSObj csObj) {
        addPointsTo(csManager.getCSVar(context, var), csObj);
    }

    @Override
    public void addVarPointsTo(Context context, Var var, Context heapContext, Obj obj) {
        addPointsTo(csManager.getCSVar(context, var), heapContext, obj);
    }

    @Override
    public void addPointerFilter(Pointer pointer, Predicate<CSObj> filter) {
        pointer.addFilter(filter);
    }

    @Override
    public void addPFGEdge(Pointer source, Pointer target, FlowKind kind,
                           Transfer transfer) {
        PointerFlowEdge edge = pointerFlowGraph.getOrAddEdge(kind, source, target);
        if (edge != null && edge.addTransfer(transfer)) {
            PointsToSet targetSet = transfer.apply(edge, getPointsToSetOf(source));
            if (!targetSet.isEmpty()) {
                addPointsTo(target, targetSet);
            }
        }
    }

    @Override
    public void addEntryPoint(EntryPoint entryPoint) {
        Context entryCtx = contextSelector.getEmptyContext();
        JMethod entryMethod = entryPoint.method();
        CSMethod csEntryMethod = csManager.getCSMethod(entryCtx, entryMethod);
        callGraph.addEntryMethod(csEntryMethod);
        addCSMethod(csEntryMethod);
        IR ir = entryMethod.getIR();
        ParamProvider paramProvider = entryPoint.paramProvider();
        // pass this objects
        if (!entryMethod.isStatic()) {
            for (Obj thisObj : paramProvider.getThisObjs()) {
                addVarPointsTo(entryCtx, ir.getThis(), entryCtx, thisObj);
            }
        }
        // pass parameter objects
        for (int i = 0; i < entryMethod.getParamCount(); ++i) {
            Var param = ir.getParam(i);
            if (propTypes.isAllowed(param)) {
                for (Obj paramObj : paramProvider.getParamObjs(i)) {
                    addVarPointsTo(entryCtx, param, entryCtx, paramObj);
                }
            }
        }
        // pass field objects
        paramProvider.getFieldObjs().forEach((base, field, obj) -> {
            CSObj csBase = csManager.getCSObj(entryCtx, base);
            InstanceField iField = csManager.getInstanceField(csBase, field);
            addPointsTo(iField, entryCtx, obj);
        });
        // pass array objects
        paramProvider.getArrayObjs().forEach((array, elem) -> {
            CSObj csArray = csManager.getCSObj(entryCtx, array);
            ArrayIndex arrayIndex = csManager.getArrayIndex(csArray);
            addPointsTo(arrayIndex, entryCtx, elem);
        });
    }

    @Override
    public void addCallEdge(Edge<CSCallSite, CSMethod> edge) {
        workList.addEntry(edge);
    }

    @Override
    public void addCSMethod(CSMethod csMethod) {
        if (callGraph.addReachableMethod(csMethod)) {
            // process new reachable context-sensitive method
            JMethod method = csMethod.getMethod();
            if (isIgnored(method)) {
                return;
            }
            processNewMethod(method);
            addStmts(csMethod, method.getIR().getStmts());
            plugin.onNewCSMethod(csMethod);
        }
    }

    @Override
    public void addStmts(CSMethod csMethod, Collection<Stmt> stmts) {
        stmtProcessor.process(csMethod, stmts);
    }

    @Override
    public void addIgnoredMethod(JMethod method) {
        ignoredMethods.add(method);
    }

    @Override
    public void initializeClass(JClass cls) {
        if (cls == null || initializedClasses.contains(cls)) {
            return;
        }
        // initialize super class
        JClass superclass = cls.getSuperClass();
        if (superclass != null) {
            initializeClass(superclass);
        }
        // TODO: initialize the superinterfaces which
        //  declare default methods
        JMethod clinit = cls.getClinit();
        if (clinit != null) {
            // addCSMethod() may trigger initialization of more
            // classes. So cls must be added before addCSMethod(),
            // otherwise, infinite recursion may occur.
            initializedClasses.add(cls);
            CSMethod csMethod = csManager.getCSMethod(
                    contextSelector.getEmptyContext(), clinit);
            addCSMethod(csMethod);
        }
    }

    @Override
    public PointerAnalysisResult getResult() {
        if (result == null) {
            result = new PointerAnalysisResultImpl(
                    propTypes, csManager, heapModel,
                    callGraph, pointerFlowGraph);
        }
        return result;
    }
}
