

package keeno.usap.analysis.pta.plugin;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.HeapModel;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.EntryPoint;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.core.solver.SpecifiedParamProvider;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.StringLiteral;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.util.collection.Sets;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static keeno.usap.util.collection.CollectionUtils.getOne;

/**
 * Models initialization of system thread group, main thread group,
 * main thread, and some Thread APIs.
 */
public class ThreadHandler implements Plugin {

    private Solver solver;

    private ClassHierarchy hierarchy;

    /**
     * This variable of Thread.start().
     */
    private Var threadStartThis;

    /**
     * Set of running threads.
     */
    private PointsToSet runningThreads;

    /**
     * Represent Thread.currentThread.
     */
    private JMethod currentThread;

    /**
     * Return variable of Thread.currentThread().
     */
    private Var currentThreadReturn;

    /**
     * Contexts of Thread.currentThread().
     */
    private final Set<Context> currentThreadContexts = Sets.newHybridSet();

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        runningThreads = solver.makePointsToSet();
        hierarchy = solver.getHierarchy();
        threadStartThis = requireNonNull(
                hierarchy.getJREMethod("<java.lang.Thread: void start()>"))
                .getIR()
                .getThis();
        currentThread = hierarchy.getJREMethod(
                "<java.lang.Thread: java.lang.Thread currentThread()>");
        currentThreadReturn = getOne(requireNonNull(currentThread)
                .getIR()
                .getReturnVars());
    }

    @Override
    public void onStart() {
        if (!solver.getOptions().getBoolean("implicit-entries")) {
            return;
        }
        TypeSystem typeSystem = solver.getTypeSystem();
        HeapModel heapModel = solver.getHeapModel();

        // setup system thread group
        JMethod threadGroupInit = requireNonNull(
                hierarchy.getJREMethod("<java.lang.ThreadGroup: void <init>()>"));
        ClassType threadGroup = typeSystem.getClassType(ClassNames.THREAD_GROUP);
        Obj systemThreadGroup = heapModel.getMockObj(Descriptor.ENTRY_DESC,
                "<system-thread-group>", threadGroup);
        solver.addEntryPoint(new EntryPoint(threadGroupInit,
                new SpecifiedParamProvider.Builder(threadGroupInit)
                        .addThisObj(systemThreadGroup)
                        .build()));

        // setup main thread group
        JMethod threadGroupInit2 = requireNonNull(
                hierarchy.getJREMethod("<java.lang.ThreadGroup: void <init>(java.lang.ThreadGroup,java.lang.String)>"));
        Obj mainThreadGroup = heapModel.getMockObj(Descriptor.ENTRY_DESC,
                "<main-thread-group>", threadGroup);
        Obj main = heapModel.getConstantObj(StringLiteral.get("main"));
        solver.addEntryPoint(new EntryPoint(threadGroupInit2,
                new SpecifiedParamProvider.Builder(threadGroupInit2)
                        .addThisObj(mainThreadGroup)
                        .addParamObj(0, systemThreadGroup)
                        .addParamObj(1, main)
                        .build()));

        // setup main thread
        JMethod threadInit = requireNonNull(
                hierarchy.getJREMethod("<java.lang.Thread: void <init>(java.lang.ThreadGroup,java.lang.String)>"));
        Obj mainThread = heapModel.getMockObj(Descriptor.ENTRY_DESC,
                "<main-thread>", typeSystem.getClassType(ClassNames.THREAD));
        solver.addEntryPoint(new EntryPoint(threadInit,
                new SpecifiedParamProvider.Builder(threadInit)
                        .addThisObj(mainThread)
                        .addParamObj(0, mainThreadGroup)
                        .addParamObj(1, main)
                        .build()));

        // The main thread is never explicitly started, which would make it a
        // RunningThread. Therefore, we make it a running thread explicitly.
        Context ctx = solver.getContextSelector().getEmptyContext();
        runningThreads.addObject(
                solver.getCSManager().getCSObj(ctx, mainThread));
    }

    @Override
    public void onNewPointsToSet(CSVar csVar, PointsToSet pts) {
        if (csVar.getVar().equals(threadStartThis)) {
            // Add new reachable thread objects to set of running threads,
            // and propagate the thread objects to return variable of
            // Thread.currentThread().
            // Since multiple threads may execute this method and
            // this.handleNewCSMethod(), we need to synchronize reads/writes
            // on runningThreads and currentThreadContexts, so we put these
            // operations in synchronized block.
            // Note that this *only* blocks when Thread.start()/@this change,
            // which is rare, thur, it should not affect concurrency much.
            synchronized (this) {
                if (runningThreads.addAll(pts)) {
                    currentThreadContexts.forEach(context ->
                            solver.addVarPointsTo(context, currentThreadReturn, pts));
                }
            }
        }
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        if (csMethod.getMethod().equals(currentThread)) {
            // When a new CS Thread.currentThread() is reachable, we propagate
            // all running threads to its return variable.
            // Ideally, we should only return the real *current* thread object,
            // which may require complicated thread analysis. So currently,
            // we just return all running threads for soundness.
            synchronized (this) {
                Context context = csMethod.getContext();
                currentThreadContexts.add(context);
                solver.addVarPointsTo(context, currentThreadReturn, runningThreads);
            }
        }
    }
}
