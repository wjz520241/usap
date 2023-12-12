

package keeno.usap.frontend.soot;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.AbstractWorldBuilder;
import keeno.usap.World;
import keeno.usap.analysis.pta.PointerAnalysis;
import keeno.usap.analysis.pta.plugin.reflection.LogItem;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.config.Options;
import keeno.usap.language.classes.ClassHierarchy;
import keeno.usap.language.classes.ClassHierarchyImpl;
import keeno.usap.language.classes.StringReps;
import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.language.type.TypeSystemImpl;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootResolver;
import soot.Transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static soot.SootClass.HIERARCHY;

public class SootWorldBuilder extends AbstractWorldBuilder {

    private static final Logger logger = LogManager.getLogger(SootWorldBuilder.class);

    /**
     * Path to the file which specifies the basic classes that should be
     * added to Scene in advance.
     */
    private static final String BASIC_CLASSES = "basic-classes.yml";

    @Override
    public void build(Options options, List<AnalysisConfig> analyses) {
        initSoot(options, analyses, this);
        // set arguments and run soot
        List<String> args = new ArrayList<>();
        // set class path
        Collections.addAll(args, "-cp", getClassPath(options));
        // set main class
        String mainClass = options.getMainClass();
        if (mainClass != null) {
            Collections.addAll(args, "-main-class", mainClass, mainClass);
        }
        // add input classes
        args.addAll(getInputClasses(options));
        runSoot(args.toArray(new String[0]));
    }

    private static void initSoot(Options options, List<AnalysisConfig> analyses,
                                 SootWorldBuilder builder) {
        // reset Soot
        G.reset();

        // set Soot options
        soot.options.Options.v().set_output_dir(
                new File(options.getOutputDir(), "sootOutput").toString());
        soot.options.Options.v().set_output_format(
                soot.options.Options.output_format_jimple);
        soot.options.Options.v().set_keep_line_number(true);
        //将分析的目标指定为应用程序。默认情况下，Soot会将分析的目标视为库或框架，通过设置为true，将目标指定为应用程序。
        soot.options.Options.v().set_app(true);
        // exclude jdk classes from application classes
        soot.options.Options.v().set_exclude(List.of("jdk.*", "apple.laf.*"));
        //将分析范围扩展到整个程序。默认情况下，Soot只分析指定的入口类及其直接依赖的类。通过设置为true，可以将分析范围扩展到整个程序，包括间接依赖的类。
        soot.options.Options.v().set_whole_program(true);
        //在分析过程中不释放方法体。默认情况下，Soot会在分析完成后释放方法体，以减少内存占用。通过设置为true，可以保留方法体的内容
        soot.options.Options.v().set_no_writeout_body_releasing(true);
        //"jb"的阶段的选项，即保留源代码中的注解。默认情况下，Soot会在分析过程中移除源代码中的注解。通过设置为"true"，可以保留源代码中的注解。
        soot.options.Options.v().setPhaseOption("jb", "preserve-source-annotations:true");
        //"jb"的阶段的选项，即禁用LambdaMetafactory模型。LambdaMetafactory是Java中用于实现Lambda表达式的工具。通过设置为"false"，可以禁用使用LambdaMetafactory模型。
        soot.options.Options.v().setPhaseOption("jb", "model-lambdametafactory:false");
        //禁用Call Graph构建
        soot.options.Options.v().setPhaseOption("cg", "enabled:false");
        if (options.isPrependJVM()) {
            // TODO: figure out why -prepend-classpath makes Soot faster
            //默认情况下，Soot会将当前类路径添加到已有的类路径之后。通过设置为true，可以将当前类路径添加到已有的类路径之前。
            soot.options.Options.v().set_prepend_classpath(true);
        }
        if (options.isAllowPhantom()) {
            //允许使用虚假引用（Phantom References）。虚假引用是Java中一种特殊类型的引用，
            // 用于实现对对象回收的处理。通过设置为true，可以允许Soot在分析过程中使用虚假引用。
            //参阅《深入理解java虚拟机》第三章 垃圾收集器与内存分配策略 3.2.3 再谈引用
            soot.options.Options.v().set_allow_phantom_refs(true);
        }
        if (options.isPreBuildIR()) {
            // 在预构建IR时，我们需要将此选项设置为false,
            // 否则Soot抛出RuntimeException
            // "No method source set for method ...".
            // TODO: figure out the reason of "No method source"
            //在加载方法体后是否删除方法体。默认情况下，Soot在加载方法体后会删除方法体，以节省内存。通过设置为false，可以保留方法体
            soot.options.Options.v().set_drop_bodies_after_load(false);
        }

        Scene scene = G.v().soot_Scene();
        addBasicClasses(scene);
        addReflectionLogClasses(analyses, scene);

        // Configure Soot transformer
        Transform transform = new Transform(
                "wjtp.tai-e", new SceneTransformer() {
            @Override
            protected void internalTransform(String phaseName, Map<String, String> opts) {
                builder.build(options, Scene.v());
            }
        });
        PackManager.v()
                .getPack("wjtp")
                .add(transform);
    }

    /**
     * 读取文件{@link #BASIC_CLASSES}指定的基类，并将它们添加到{@code scene}中
     */
    private static void addBasicClasses(Scene scene) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JavaType type = mapper.getTypeFactory()
                .constructCollectionType(List.class, String.class);
        try {
            InputStream content = SootWorldBuilder.class
                    .getClassLoader()
                    .getResourceAsStream(BASIC_CLASSES);
            List<String> classNames = mapper.readValue(content, type);
            classNames.forEach(name -> scene.addBasicClass(name, HIERARCHY));
        } catch (IOException e) {
            throw new SootFrontendException("Failed to read Soot basic classes", e);
        }
    }

    /**
     * 将反射日志中的类添加到场景中。Tai-e的ClassHierarchy依赖于Soot的Scene，
     * 在构建层次结构后不会发生变化，因此我们需要在启动Soot之前将类添加到反射日志中。
     * <p>
     * TODO: this is a tentative solution. We should remove it and use other
     *  way to load basic classes in the reflection log, so that world builder
     *  does not depend on analyses to be executed.
     *
     * @param analyses the analyses to be executed
     * @param scene    the Soot's scene
     */
    private static void addReflectionLogClasses(List<AnalysisConfig> analyses, Scene scene) {
        analyses.forEach(config -> {
            if (config.getId().equals(PointerAnalysis.ID)) {
                String path = config.getOptions().getString("reflection-log");
                if (path != null) {
                    LogItem.load(path).forEach(item -> {
                        // add target class
                        String target = item.target;
                        String targetClass;
                        if (target.startsWith("<")) {
                            targetClass = StringReps.getClassNameOf(target);
                        } else {
                            targetClass = target;
                        }
                        if (StringReps.isArrayType(targetClass)) {
                            targetClass = StringReps.getBaseTypeNameOf(target);
                        }
                        if (!PrimitiveType.isPrimitiveType(targetClass)) {
                            scene.addBasicClass(targetClass);
                        }
                    });
                }
            }
        });
    }

    private void build(Options options, Scene scene) {
        World.reset();
        World world = new World();
        World.set(world);

        // options will be used during World building, thus it should be
        // set at first.
        world.setOptions(options);
        // initialize class hierarchy
        ClassHierarchy hierarchy = new ClassHierarchyImpl();
        SootClassLoader loader = new SootClassLoader(
                scene, hierarchy, options.isAllowPhantom());
        hierarchy.setDefaultClassLoader(loader);
        hierarchy.setBootstrapClassLoader(loader);
        world.setClassHierarchy(hierarchy);
        // initialize type manager
        TypeSystem typeSystem = new TypeSystemImpl(hierarchy);
        world.setTypeSystem(typeSystem);
        // initialize converter
        Converter converter = new Converter(loader, typeSystem);
        loader.setConverter(converter);
        // build classes in hierarchy
        buildClasses(hierarchy, scene);
        // set main method
        if (options.getMainClass() != null) {
            if (scene.hasMainClass()) {
                world.setMainMethod(
                        converter.convertMethod(scene.getMainMethod()));
            } else {
                logger.warn("Warning: main class '{}'" +
                                " does not have main(String[]) method!",
                        options.getMainClass());
            }
        } else {
            logger.warn("Warning: main class was not given!");
        }
        // set implicit entries
        world.setImplicitEntries(implicitEntries.stream()
                .map(hierarchy::getJREMethod)
                // some implicit entries may not exist in certain JDK version,
                // thus we filter out null
                .filter(Objects::nonNull)
                .toList());
        // initialize IR builder
        world.setNativeModel(getNativeModel(typeSystem, hierarchy, options));
        IRBuilder irBuilder = new IRBuilder(converter);
        world.setIRBuilder(irBuilder);
        if (options.isPreBuildIR()) {
            irBuilder.buildAll(hierarchy);
        }
    }

    protected static void buildClasses(ClassHierarchy hierarchy, Scene scene) {
        // TODO: parallelize?
        new ArrayList<>(scene.getClasses()).forEach(c ->
                hierarchy.getDefaultClassLoader().loadClass(c.getName()));
    }

    private static void runSoot(String[] args) {
        try {
            soot.Main.v().run(args);
        } catch (SootResolver.SootClassNotFoundException e) {
            throw new RuntimeException(e.getMessage()
                    .replace("is your soot-class-path set",
                            "are your class path and class name given"));
        } catch (AssertionError e) {
            if (e.getStackTrace()[0].toString()
                    .startsWith("soot.SootResolver.resolveClass")) {
                throw new RuntimeException("Exception thrown by class resolver," +
                        " are your class path and class name given properly?", e);
            }
            throw e;
        } catch (Exception e) {
            if (e.getStackTrace()[0].getClassName().startsWith("soot.JastAdd")) {
                throw new RuntimeException("""
                        Soot frontend failed to parse input Java source file(s).
                        This exception may be caused by:
                        1. syntax or semantic errors in the source code. In this case, please fix the errors.
                        2. language features introduced by Java 8+ in the source code.
                           In this case, you could either compile the source code to bytecode (*.class)
                           or rewrite the code by using old features.""", e);
            }
            throw e;
        }
    }
}
