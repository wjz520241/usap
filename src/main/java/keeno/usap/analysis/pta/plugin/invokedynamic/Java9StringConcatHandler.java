

package keeno.usap.analysis.pta.plugin.invokedynamic;

import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.Plugin;
import keeno.usap.ir.exp.InvokeDynamic;
import keeno.usap.ir.exp.InvokeVirtual;
import keeno.usap.ir.exp.NewInstance;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.New;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.classes.Signatures;
import keeno.usap.language.classes.Subsignature;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.util.collection.Maps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Since Java 9, String concatenation is implemented via invokedynamic.
 * This class handles string concatenation, and
 * see <a href="https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/StringConcatFactory.html">the documentation</a>
 * for details.
 */
public class Java9StringConcatHandler implements Plugin {

    private Solver solver;

    private ClassType stringBuilder;

    private ClassType string;

    private MethodRef appendString;

    private MethodRef appendObject;

    private MethodRef toString;

    /**
     * Counter for naming temporary variables.
     */
    private int counter = 0;

    private final Map<JMethod, Collection<Stmt>> method2GenStmts = Maps.newHybridMap();

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        TypeSystem typeSystem = solver.getTypeSystem();
        stringBuilder = typeSystem.getClassType(ClassNames.STRING_BUILDER);
        string = typeSystem.getClassType(ClassNames.STRING);
        JClass sb = stringBuilder.getJClass();
        appendString = Objects.requireNonNull(sb.getDeclaredMethod(Subsignature.get(
                        "java.lang.StringBuilder append(java.lang.String)")))
                .getRef();
        appendObject = Objects.requireNonNull(sb.getDeclaredMethod(Subsignature.get(
                        "java.lang.StringBuilder append(java.lang.Object)")))
                .getRef();
        toString = Objects.requireNonNull(sb.getDeclaredMethod(Subsignature.get(
                        "java.lang.String toString()")))
                .getRef();
    }

    /**
     * @return {@code true} if given {@code invoke} is an invokedynamic to
     * StringConcatFactory.makeConcatWithConstants(...).
     */
    static boolean isStringConcatFactoryMake(Invoke invoke) {
        // Declaring class of bootstrap method reference may be phantom
        // (looks like an issue of current (soot-based) front end),
        // thus we use resolveNullable() to avoid exception.
        // TODO: change to resolve() after using new front end
        JMethod bsm = ((InvokeDynamic) invoke.getInvokeExp())
                .getBootstrapMethodRef()
                .resolveNullable();
        if (bsm != null) {
            String bsmSig = bsm.getSignature();
            return bsmSig.equals(Signatures.STRING_CONCAT_FACTORY_MAKE);
        } else {
            return false;
        }
    }

    @Override
    public void onNewStmt(Stmt stmt, JMethod container) {
        if (stmt instanceof Invoke invoke &&
                invoke.isDynamic() &&
                isStringConcatFactoryMake(invoke) &&
                invoke.getResult() != null) {
            List<Stmt> stmts = generate(invoke, container);
            method2GenStmts.computeIfAbsent(container, __ -> new ArrayList<>())
                    .addAll(stmts);
        }
    }

    /**
     * Generates modelling statements for given invocation site of
     * StringConcatFactory.makeConcatWithConstants(...).
     * Currently, we only consider concatenation of reference types,
     * and ignore string constants and primitive types.
     */
    private List<Stmt> generate(Invoke stringConcatMake, JMethod container) {
        //
        // generate invocations to model string concatenation
        List<Stmt> stmts = new ArrayList<>();
        // generate sb = new StringBuilder;
        Var sbVar = getTempVar(container);
        stmts.add(new New(container, sbVar, new NewInstance(stringBuilder)));
        // generate sb.append(...);
        InvokeDynamic indy = (InvokeDynamic) stringConcatMake.getInvokeExp();
        indy.getArgs().forEach(arg -> {
            if (arg.getType() instanceof ClassType argType) {
                MethodRef append = argType.equals(string) ? appendString : appendObject;
                stmts.add(new Invoke(container,
                        new InvokeVirtual(append, sbVar, List.of(arg))));
            }
        });
        // generate string = sb.toString();
        Var string = stringConcatMake.getResult();
        stmts.add(new Invoke(container,
                new InvokeVirtual(toString, sbVar, List.of()),
                string));
        return stmts;
    }

    private Var getTempVar(JMethod container) {
        String varName = "%stringconcat-" + counter++;
        return new Var(container, varName, stringBuilder, -1);
    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        Collection<Stmt> stmts = method2GenStmts.get(csMethod.getMethod());
        if (stmts != null) {
            solver.addStmts(csMethod, stmts);
        }
    }
}
