

package keeno.usap.analysis;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.analysis.graph.callgraph.CallGraph;
import keeno.usap.analysis.graph.callgraph.CallGraphBuilder;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.config.ConfigException;
import keeno.usap.config.Plan;
import keeno.usap.config.Scope;
import keeno.usap.ir.IR;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.AnalysisException;
import keeno.usap.util.Timer;
import keeno.usap.util.graph.SimpleGraph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates and executes analyses based on given analysis plan.
 */
public class AnalysisManager {

    private static final Logger logger = LogManager.getLogger(AnalysisManager.class);

    private final Plan plan;

    /**
     * Whether keep results of all analyses. If the value is {@code false},
     * this manager will clear unused results after it finishes each analysis.
     */
    private final boolean keepAllResults;

    /**
     * Graph that describes the dependencies among analyses (represented
     * by their IDs) in the plan. This graph is used to check whether
     * certain analysis results are useful.
     */
    private SimpleGraph<String> dependenceGraph;

    /**
     * List of analyses that have been executed. For an element in this list,
     * once its result is clear, it will also be removed from this list.
     */
    private List<Analysis> executedAnalyses;

    private List<JClass> classScope;

    private List<JMethod> methodScope;

    public AnalysisManager(Plan plan) {
        this.plan = plan;
        this.keepAllResults = plan.keepResult().contains(Plan.KEEP_ALL);
    }

    /**
     * Executes the analysis plan.
     */
    public void execute() {
        // initialize
        if (!keepAllResults) {
            dependenceGraph = new SimpleGraph<>();
            for (AnalysisConfig c : plan.dependenceGraph()) {
                for (AnalysisConfig succ : plan.dependenceGraph().getSuccsOf(c)) {
                    dependenceGraph.addEdge(c.getId(), succ.getId());
                }
            }
            executedAnalyses = new ArrayList<>();
        }
        classScope = null;
        methodScope = null;
        // execute analyses
        plan.analyses().forEach(config -> {
            Analysis analysis = Timer.runAndCount(
                    () -> runAnalysis(config), config.getId(), Level.INFO);
            if (!keepAllResults) {
                executedAnalyses.add(analysis);
                clearUnusedResults(analysis);
            }
        });
    }

    private Analysis runAnalysis(AnalysisConfig config) {
        Analysis analysis;
        // Create analysis instance
        try {
            Class<?> clazz = Class.forName(config.getAnalysisClass());
            Constructor<?> ctor = clazz.getConstructor(AnalysisConfig.class);
            analysis = (Analysis) ctor.newInstance(config);
        } catch (ClassNotFoundException e) {
            throw new AnalysisException("Analysis class " +
                    config.getAnalysisClass() + " is not found", e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AnalysisException("Failed to get constructor " +
                    config.getAnalysisClass() + "(AnalysisConfig), " +
                    "thus the analysis cannot be executed by Tai-e", e);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new AnalysisException("Failed to initialize " +
                    config.getAnalysisClass(), e);
        } catch (ClassCastException e) {
            throw new ConfigException(
                    config.getAnalysisClass() + " is not an analysis class");
        }
        // Run the analysis
        if (analysis instanceof ProgramAnalysis<?> pa) {
            runProgramAnalysis(pa);
        } else if (analysis instanceof ClassAnalysis<?> ca) {
            runClassAnalysis(ca);
        } else if (analysis instanceof MethodAnalysis<?> ma) {
            runMethodAnalysis(ma);
        } else {
            throw new ConfigException(config.getAnalysisClass() +
                    " is not a supported analysis class");
        }
        return analysis;
    }

    private void runProgramAnalysis(ProgramAnalysis<?> analysis) {
        Object result = analysis.analyze();
        if (result != null) {
            World.get().storeResult(analysis.getId(), result);
        }
    }

    private void runClassAnalysis(ClassAnalysis<?> analysis) {
        getClassScope().parallelStream()
                .forEach(c -> {
                    Object result = analysis.analyze(c);
                    if (result != null) {
                        c.storeResult(analysis.getId(), result);
                    }
                });
    }

    private List<JClass> getClassScope() {
        if (classScope == null) {
            Scope scope = World.get().getOptions().getScope();
            classScope = switch (scope) {
                case APP -> World.get()
                        .getClassHierarchy()
                        .applicationClasses()
                        .toList();
                case ALL -> World.get()
                        .getClassHierarchy()
                        .allClasses()
                        .toList();
                case REACHABLE -> {
                    CallGraph<?, JMethod> callGraph = World.get().getResult(CallGraphBuilder.ID);
                    yield callGraph.reachableMethods()
                            .map(JMethod::getDeclaringClass)
                            .distinct()
                            .toList();
                }
            };
            logger.info("{} classes in scope ({}) of class analyses",
                    classScope.size(), scope);
        }
        return classScope;
    }

    private void runMethodAnalysis(MethodAnalysis<?> analysis) {
        getMethodScope()
                .parallelStream()
                .forEach(m -> {
                    IR ir = m.getIR();
                    Object result = analysis.analyze(ir);
                    if (result != null) {
                        ir.storeResult(analysis.getId(), result);
                    }
                });
    }

    private List<JMethod> getMethodScope() {
        if (methodScope == null) {
            Scope scope = World.get().getOptions().getScope();
            methodScope = switch (scope) {
                case APP, ALL -> getClassScope()
                        .stream()
                        .map(JClass::getDeclaredMethods)
                        .flatMap(Collection::stream)
                        .filter(m -> !m.isAbstract())
                        .toList();
                case REACHABLE -> {
                    CallGraph<?, JMethod> callGraph = World.get().getResult(CallGraphBuilder.ID);
                    yield callGraph.reachableMethods().toList();
                }
            };
            logger.info("{} methods in scope ({}) of method analyses",
                    methodScope.size(), scope);
        }
        return methodScope;
    }

    /**
     * @param analysis the analysis that just finished.
     */
    private void clearUnusedResults(Analysis analysis) {
        // analysis has finished, we can remove its dependencies
        // copy in-edges to a new list to avoid concurrent modifications
        var edgesToRemove = new ArrayList<>(
                dependenceGraph.getInEdgesOf(analysis.getId()));
        edgesToRemove.forEach(e ->
                dependenceGraph.removeEdge(e.source(), e.target()));
        // select the analyses whose results are unused and not in keepResult
        List<String> unused = executedAnalyses.stream()
                .map(Analysis::getId)
                .filter(id -> dependenceGraph.getOutDegreeOf(id) == 0)
                .filter(id -> !plan.keepResult().contains(id))
                .toList();
        if (!unused.isEmpty()) {
            logger.info("Clearing unused results of {}", unused);
            for (String id : unused) {
                int i;
                for (i = 0; i < executedAnalyses.size(); ++i) {
                    Analysis a = executedAnalyses.get(i);
                    if (a.getId().equals(id)) {
                        if (a instanceof ProgramAnalysis) {
                            World.get().clearResult(id);
                        } else if (a instanceof ClassAnalysis) {
                            getClassScope().forEach(c -> c.clearResult(id));
                        } else if (a instanceof MethodAnalysis) {
                            getMethodScope().forEach(m -> m.getIR().clearResult(id));
                        }
                        break;
                    }
                }
                executedAnalyses.remove(i);
            }
        }
    }
}
