

package keeno.usap.analysis.pta.plugin.reflection;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.AbstractModel;
import keeno.usap.analysis.pta.plugin.util.Reflections;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.AnalysisException;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Base class for reflection inference.
 * This class provides methods to handle class- and member-retrieving APIs for
 * the cases where the both the name and the receiver class object are known.
 * TODO: take ClassLoader.loadClass(...) into account.
 */
abstract class InferenceModel extends AbstractModel {

    protected final MetaObjHelper helper;

    protected final Set<Invoke> invokesWithLog;

    private final MultiMap<Invoke, JClass> forNameTargets = Maps.newMultiMap();

    InferenceModel(Solver solver, MetaObjHelper helper, Set<Invoke> invokesWithLog) {
        super(solver);
        this.helper = helper;
        this.invokesWithLog = invokesWithLog;
    }

    protected void classForNameKnown(
            Context context, Invoke forName, @Nullable String className) {
        if (className != null) {
            JClass clazz = hierarchy.getClass(className);
            if (clazz != null) {
                solver.initializeClass(clazz);
                Var result = forName.getResult();
                if (result != null) {
                    Obj classObj = helper.getMetaObj(clazz);
                    solver.addVarPointsTo(context, result, classObj);
                    forNameTargets.put(forName, clazz);
                }
            }
        }
    }

    MultiMap<Invoke, JClass> getForNameTargets() {
        return forNameTargets;
    }

    protected void classGetConstructorKnown(
            Context context, Invoke invoke, @Nullable JClass clazz) {
        if (clazz != null) {
            Var result = invoke.getResult();
            if (result != null) {
                Stream<JMethod> constructors = switch (invoke.getMethodRef().getName()) {
                    case "getConstructor" -> Reflections.getConstructors(clazz);
                    case "getDeclaredConstructor" -> Reflections.getDeclaredConstructors(clazz);
                    default -> throw new AnalysisException(
                            "Expected [getConstructor, getDeclaredConstructor], given " +
                                    invoke.getMethodRef());
                };
                constructors.map(helper::getMetaObj)
                        .forEach(ctorObj -> solver.addVarPointsTo(context, result, ctorObj));
            }
        }
    }

    protected void classGetMethodKnown(Context context, Invoke invoke,
                                       @Nullable JClass clazz, @Nullable String name) {
        if (clazz != null && name != null) {
            Var result = invoke.getResult();
            if (result != null) {
                Stream<JMethod> methods = switch (invoke.getMethodRef().getName()) {
                    case "getMethod" -> Reflections.getMethods(clazz, name);
                    case "getDeclaredMethod" -> Reflections.getDeclaredMethods(clazz, name);
                    default -> throw new AnalysisException(
                            "Expected [getMethod, getDeclaredMethod], given " +
                                    invoke.getMethodRef());
                };
                methods.map(helper::getMetaObj)
                        .forEach(mtdObj -> solver.addVarPointsTo(context, result, mtdObj));
            }
        }
    }

    protected abstract void handleNewNonInvokeStmt(Stmt stmt);
}
