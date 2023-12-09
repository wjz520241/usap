

package keeno.usap.analysis.pta;

import org.apache.logging.log4j.Level;
import keeno.usap.World;
import keeno.usap.analysis.ProgramAnalysis;
import keeno.usap.analysis.pta.core.cs.element.MapBasedCSManager;
import keeno.usap.analysis.pta.core.cs.selector.ContextSelector;
import keeno.usap.analysis.pta.core.cs.selector.ContextSelectorFactory;
import keeno.usap.analysis.pta.core.heap.AllocationSiteBasedModel;
import keeno.usap.analysis.pta.core.heap.HeapModel;
import keeno.usap.analysis.pta.core.solver.DefaultSolver;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.AnalysisTimer;
import keeno.usap.analysis.pta.plugin.ClassInitializer;
import keeno.usap.analysis.pta.plugin.CompositePlugin;
import keeno.usap.analysis.pta.plugin.EntryPointHandler;
import keeno.usap.analysis.pta.plugin.Plugin;
import keeno.usap.analysis.pta.plugin.ReferenceHandler;
import keeno.usap.analysis.pta.plugin.ResultProcessor;
import keeno.usap.analysis.pta.plugin.ThreadHandler;
import keeno.usap.analysis.pta.plugin.exception.ExceptionAnalysis;
import keeno.usap.analysis.pta.plugin.invokedynamic.InvokeDynamicAnalysis;
import keeno.usap.analysis.pta.plugin.invokedynamic.Java9StringConcatHandler;
import keeno.usap.analysis.pta.plugin.invokedynamic.LambdaAnalysis;
import keeno.usap.analysis.pta.plugin.natives.NativeModeller;
import keeno.usap.analysis.pta.plugin.reflection.ReflectionAnalysis;
import keeno.usap.analysis.pta.plugin.taint.TaintAnalysis;
import keeno.usap.analysis.pta.toolkit.CollectionMethods;
import keeno.usap.analysis.pta.toolkit.mahjong.Mahjong;
import keeno.usap.analysis.pta.toolkit.scaler.Scaler;
import keeno.usap.analysis.pta.toolkit.zipper.Zipper;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.config.AnalysisOptions;
import keeno.usap.config.ConfigException;
import keeno.usap.util.AnalysisException;
import keeno.usap.util.Timer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PointerAnalysis extends ProgramAnalysis<PointerAnalysisResult> {

    public static final String ID = "pta";

    public PointerAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public PointerAnalysisResult analyze() {
        AnalysisOptions options = getOptions();
        HeapModel heapModel = new AllocationSiteBasedModel(options);
        ContextSelector selector = null;
        String advanced = options.getString("advanced");
        String cs = options.getString("cs");
        if (advanced != null) {
            if (advanced.equals("collection")) {
                selector = ContextSelectorFactory.makeSelectiveSelector(cs,
                        new CollectionMethods(World.get().getClassHierarchy()).get());
            } else {
                // run context-insensitive analysis as pre-analysis
                PointerAnalysisResult preResult = runAnalysis(heapModel,
                        ContextSelectorFactory.makeCISelector());
                if (advanced.startsWith("scaler")) {
                    selector = Timer.runAndCount(() -> ContextSelectorFactory
                                    .makeGuidedSelector(Scaler.run(preResult, advanced)),
                            "Scaler", Level.INFO);
                } else if (advanced.startsWith("zipper")) {
                    selector = Timer.runAndCount(() -> ContextSelectorFactory
                                    .makeSelectiveSelector(cs, Zipper.run(preResult, advanced)),
                            "Zipper", Level.INFO);
                } else if (advanced.equals("mahjong")) {
                    heapModel = Timer.runAndCount(() -> Mahjong.run(preResult, options),
                            "Mahjong", Level.INFO);
                } else {
                    throw new IllegalArgumentException(
                            "Illegal advanced analysis argument: " + advanced);
                }
            }
        }
        if (selector == null) {
            selector = ContextSelectorFactory.makePlainSelector(cs);
        }
        return runAnalysis(heapModel, selector);
    }

    private PointerAnalysisResult runAnalysis(HeapModel heapModel,
                                              ContextSelector selector) {
        AnalysisOptions options = getOptions();
        Solver solver = new DefaultSolver(options,
                heapModel, selector, new MapBasedCSManager());
        // The initialization of some Plugins may read the fields in solver,
        // e.g., contextSelector or csManager, thus we initialize Plugins
        // after setting all other fields of solver.
        setPlugin(solver, options);
        solver.solve();
        return solver.getResult();
    }

    private static void setPlugin(Solver solver, AnalysisOptions options) {
        CompositePlugin plugin = new CompositePlugin();
        // add builtin plugins
        // To record elapsed time precisely, AnalysisTimer should be added at first.
        plugin.addPlugin(
                new AnalysisTimer(),
                new EntryPointHandler(),
                new ClassInitializer(),
                new ThreadHandler(),
                new NativeModeller(),
                new ExceptionAnalysis()
        );
        int javaVersion = World.get().getOptions().getJavaVersion();
        if (javaVersion < 9) {
            // current reference handler doesn't support Java 9+
            plugin.addPlugin(new ReferenceHandler());
        }
        if (javaVersion >= 8) {
            plugin.addPlugin(new LambdaAnalysis());
        }
        if (javaVersion >= 9) {
            plugin.addPlugin(new Java9StringConcatHandler());
        }
        if (options.getString("reflection-inference") != null ||
                options.getString("reflection-log") != null) {
            plugin.addPlugin(new ReflectionAnalysis());
        }
        if (options.getBoolean("handle-invokedynamic") &&
                InvokeDynamicAnalysis.useMethodHandle()) {
            plugin.addPlugin(new InvokeDynamicAnalysis());
        }
        if (options.getString("taint-config") != null) {
            plugin.addPlugin(new TaintAnalysis());
        }
        plugin.addPlugin(new ResultProcessor());
        // add plugins specified in options
        // noinspection unchecked
        addPlugins(plugin, (List<String>) options.get("plugins"));
        // connects plugins and solver
        plugin.setSolver(solver);
        solver.setPlugin(plugin);
    }

    private static void addPlugins(CompositePlugin plugin,
                                   List<String> pluginClasses) {
        for (String pluginClass : pluginClasses) {
            try {
                Class<?> clazz = Class.forName(pluginClass);
                Constructor<?> ctor = clazz.getConstructor();
                Plugin newPlugin = (Plugin) ctor.newInstance();
                plugin.addPlugin(newPlugin);
            } catch (ClassNotFoundException e) {
                throw new ConfigException(
                        "Plugin class " + pluginClass + " is not found");
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new AnalysisException("Failed to get constructor of " +
                        pluginClass + ", does the plugin class" +
                        " provide a public non-arg constructor?");
            } catch (InvocationTargetException | InstantiationException e) {
                throw new AnalysisException(
                        "Failed to create plugin instance for " + pluginClass, e);
            }
        }
    }
}
