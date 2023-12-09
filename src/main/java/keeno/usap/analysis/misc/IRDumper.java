

package keeno.usap.analysis.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.analysis.ClassAnalysis;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.IR;
import keeno.usap.ir.IRPrinter;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.annotation.Annotation;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.classes.Modifier;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Maps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Dumps Tai-e IR for classes of input program.
 */
public class IRDumper extends ClassAnalysis<Void> {

    public static final String ID = "ir-dumper";

    private static final Logger logger = LogManager.getLogger(IRDumper.class);

    private static final String IR_DIR = "tir";

    private static final String SUFFIX = ".tir";

    private static final String INDENT = "    ";

    /**
     * Directory to dump IR.
     */
    private final File dumpDir;

    public IRDumper(AnalysisConfig config) {
        super(config);
        dumpDir = new File(World.get().getOptions().getOutputDir(), IR_DIR);
        if (!dumpDir.exists()) {
            dumpDir.mkdirs();
        }
        logger.info("Dumping IR in {}", dumpDir.getAbsolutePath());
    }

    @Override
    public Void analyze(JClass jclass) {
        new Dumper(dumpDir, jclass).dump();
        return null;
    }

    private static class Dumper {

        private final File dumpDir;

        private final JClass jclass;

        private PrintStream out;

        private Dumper(File dumpDir, JClass jclass) {
            this.dumpDir = dumpDir;
            this.jclass = jclass;
        }

        private void dump() {
            String fileName = jclass.getName() + SUFFIX;
            try (PrintStream out = new PrintStream(new FileOutputStream(
                    new File(dumpDir, fileName)))) {
                this.out = out;
                dumpClassDeclaration();
                out.println(" {");
                out.println();
                if (!jclass.getDeclaredFields().isEmpty()) {
                    jclass.getDeclaredFields().forEach(this::dumpField);
                }
                jclass.getDeclaredMethods().forEach(this::dumpMethod);
                out.println("}");
            } catch (FileNotFoundException e) {
                logger.warn("Failed to dump class {}", jclass, e);
            }
        }

        private void dumpClassDeclaration() {
            // dump annotations
            jclass.getAnnotations().forEach(out::println);
            // dump class modifiers
            jclass.getModifiers()
                    .stream()
                    // if jclass is an interface, then don't dump modifiers
                    // "interface" and "abstract"
                    .filter(m -> !jclass.isInterface() ||
                            (m != Modifier.INTERFACE && m != Modifier.ABSTRACT))
                    .forEach(m -> out.print(m + " "));
            // dump class type
            if (jclass.isInterface()) {
                out.print("interface");
            } else {
                out.print("class");
            }
            out.print(' ');
            // dump class name
            out.print(jclass.getName());
            // dump super class
            JClass superClass = jclass.getSuperClass();
            if (superClass != null) {
                out.print(" extends ");
                out.print(superClass.getName());
            }
            // dump interfaces
            if (!jclass.getInterfaces().isEmpty()) {
                out.print(" implements ");
                out.print(jclass.getInterfaces()
                        .stream()
                        .map(JClass::getName)
                        .collect(Collectors.joining(", ")));
            }
        }

        private void dumpField(JField field) {
            for (Annotation annotation : field.getAnnotations()) {
                out.print(INDENT);
                out.println(annotation);
            }
            out.print(INDENT);
            dumpModifiers(field.getModifiers());
            out.printf("%s %s;%n%n", field.getType().getName(), field.getName());
        }

        private void dumpModifiers(Set<Modifier> mods) {
            mods.forEach(m -> out.print(m + " "));
        }

        private void dumpMethod(JMethod method) {
            for (Annotation annotation : method.getAnnotations()) {
                out.print(INDENT);
                out.println(annotation);
            }
            out.print(INDENT);
            dumpMethodDeclaration(method);
            if (hasIR(method)) {
                out.println(" {");
                IR ir = method.getIR();
                // dump variables
                dumpVariables(ir);
                // dump statements
                ir.forEach(s -> out.printf("%s%s%s%n",
                        INDENT, INDENT, IRPrinter.toString(s)));
                // dump exception entries
                if (!ir.getExceptionEntries().isEmpty()) {
                    out.println();
                    ir.getExceptionEntries().forEach(e ->
                            out.printf("%s%s%s%n", INDENT, INDENT, e));
                }
                out.printf("%s}%n", INDENT);
            } else {
                out.println(";");
            }
            out.println();
        }

        private void dumpMethodDeclaration(JMethod method) {
            dumpModifiers(method.getModifiers());
            out.printf("%s %s", method.getReturnType(), method.getName());
            // dump parameters
            StringJoiner paramsJoiner = new StringJoiner(", ", "(", ")");
            for (int i = 0; i < method.getParamCount(); ++i) {
                StringJoiner joiner = new StringJoiner(" ");
                method.getParamAnnotations(i)
                        .forEach(anno -> joiner.add(anno.toString()));
                joiner.add(method.getParamType(i).getName());
                // if the method has explicit parameter names
                // or the method has IR, then dump parameter names
                String paramName = method.getParamName(i);
                if (paramName != null) {
                    joiner.add(paramName);
                } else if (hasIR(method)) {
                    joiner.add(method.getIR().getParam(i).getName());
                }
                paramsJoiner.add(joiner.toString());
            }
            out.print(paramsJoiner);
        }

        private static boolean hasIR(JMethod method) {
            return !method.isAbstract();
        }

        private void dumpVariables(IR ir) {
            // group variables by their types;
            Map<Type, List<Var>> vars = Maps.newLinkedHashMap();
            ir.getVars().stream()
                    .filter(v -> v != ir.getThis())
                    .filter(v -> !ir.getParams().contains(v))
                    .forEach(v -> vars.computeIfAbsent(v.getType(),
                                    __ -> new ArrayList<>())
                            .add(v));
            vars.forEach((t, vs) -> {
                out.printf("%s%s%s ", INDENT, INDENT, t);
                out.print(vs.stream()
                        .map(Var::getName)
                        .collect(Collectors.joining(", ")));
                out.println(";");
            });
        }
    }
}
