

package keeno.usap.analysis.pta.plugin.reflection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSMethod;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.InvokeUtils;
import keeno.usap.analysis.pta.plugin.util.SolverHolder;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.classes.ClassMember;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.PrimitiveType;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static keeno.usap.analysis.pta.plugin.util.InvokeUtils.BASE;

class LogBasedModel extends SolverHolder {

    private static final Logger logger = LogManager.getLogger(LogBasedModel.class);

    private final Set<String> supportedApis = Set.of(
            "Class.forName",
            "Class.newInstance",
            "Constructor.newInstance",
            "Method.invoke",
            "Field.get",
            "Field.set",
            "Array.newInstance"
    );

    private final Map<String, String> fullNames = Map.of(
            "Class", ClassNames.CLASS,
            "Constructor", ClassNames.CONSTRUCTOR,
            "Method", ClassNames.METHOD,
            "Field", ClassNames.FIELD,
            "Array", ClassNames.ARRAY
    );

    private final MetaObjHelper helper;

    private final Set<Invoke> loggedInvokes = Sets.newSet();

    private final Set<JMethod> relevantMethods = Sets.newSet();

    /**
     * Targets for Class.forName(...).
     */
    private final MultiMap<Invoke, JClass> forNameTargets = Maps.newMultiMap();

    /**
     * Targets for Class.newInstance().
     */
    private final MultiMap<Invoke, JClass> classTargets = Maps.newMultiMap();

    /**
     * Targets for Constructor.newInstance(...), Method.invoke(...), and Field.get/set(...).
     */
    private final MultiMap<Invoke, ClassMember> memberTargets = Maps.newMultiMap();

    /**
     * Targets for Array.newInstance(...).
     */
    private final MultiMap<Invoke, ClassType> arrayTypeTargets = Maps.newMultiMap();

    /**
     * Callers or targets that are absent in the closed world.
     */
    private final Set<String> missingItems = Sets.newSet();

    LogBasedModel(Solver solver, MetaObjHelper helper, String logPath) {
        super(solver);
        this.helper = helper;
        logger.info("Using reflection log from {}",
                Path.of(logPath).toAbsolutePath());
        LogItem.load(logPath).forEach(this::addItem);
    }

    private void addItem(LogItem item) {
        if (!supportedApis.contains(item.api)) {
            return;
        }
        // obtain reflective target
        Object target = switch (item.api) {
            case "Class.forName", "Class.newInstance" -> hierarchy.getClass(item.target);
            case "Constructor.newInstance", "Method.invoke" -> hierarchy.getMethod(item.target);
            case "Field.get", "Field.set" -> hierarchy.getField(item.target);
            case "Array.newInstance" -> typeSystem.getType(item.target);
            default -> null;
        };
        // ignore get/set of fields of primitive types
        if (target instanceof JField field &&
                field.getType() instanceof PrimitiveType) {
            return;
        }
        // add target specified in the item
        if (target != null) {
            List<Invoke> invokes = getMatchedInvokes(item);
            if (target instanceof JClass jclass) {
                if (item.api.equals("Class.forName")) {
                    for (Invoke invoke : invokes) {
                        forNameTargets.put(invoke, jclass);
                    }
                } else {
                    for (Invoke invoke : invokes) {
                        classTargets.put(invoke, jclass);
                    }
                }
            } else if (target instanceof ClassMember member) {
                for (Invoke invoke : invokes) {
                    memberTargets.put(invoke, member);
                }
            } else if (target instanceof ArrayType arrayType) {
                // Note that currently we only support Array.newInstance(Class,int),
                // and ignore primitive arrays.
                if (arrayType.baseType() instanceof ClassType baseClass) {
                    for (Invoke invoke : invokes) {
                        arrayTypeTargets.put(invoke, baseClass);
                    }
                }
            }
            invokes.forEach(invoke -> {
                loggedInvokes.add(invoke);
                relevantMethods.add(invoke.getContainer());
            });
        } else if (missingItems.add(item.target)) {
            logger.warn("Reflective target '{}' for {} is not found", item.target, item.api);
        }
    }

    private List<Invoke> getMatchedInvokes(LogItem item) {
        int lastDot = item.caller.lastIndexOf('.');
        String callerClass = item.caller.substring(0, lastDot);
        String callerMethod = item.caller.substring(lastDot + 1);
        JClass clazz = hierarchy.getClass(callerClass);
        if (clazz == null) {
            if (missingItems.add(callerClass)) {
                logger.warn("Reflective caller class '{}' is absent", callerClass);
            }
            return List.of();
        }
        List<Invoke> invokes = new ArrayList<>();
        clazz.getDeclaredMethods()
                .stream()
                .filter(m -> m.getName().equals(callerMethod) && !m.isAbstract())
                .forEach(caller ->
                        caller.getIR()
                                .invokes(false)
                                .filter(invoke -> isMatched(item, invoke))
                                .forEach(invokes::add));
        if (invokes.isEmpty()) {
            logger.warn("No matched invokes found for {}/{}",
                    item.caller, item.lineNumber);
        }
        return invokes;
    }

    private boolean isMatched(LogItem item, Invoke invoke) {
        int lastDot = item.api.lastIndexOf('.');
        String apiClass = fullNames.get(item.api.substring(0, lastDot));
        String apiMethod = item.api.substring(lastDot + 1);
        JMethod callee = invoke.getMethodRef().resolve();
        return callee.getDeclaringClass().getName().equals(apiClass) &&
                callee.getName().equals(apiMethod) &&
                (item.lineNumber == LogItem.UNKNOWN ||
                        item.lineNumber == invoke.getLineNumber());
    }

    /**
     * @return set of reflective invocations that are annotated by the log.
     */
    Set<Invoke> getInvokesWithLog() {
        return Collections.unmodifiableSet(loggedInvokes);
    }

    MultiMap<Invoke, JClass> getForNameTargets() {
        return forNameTargets;
    }

    void handleNewCSMethod(CSMethod csMethod) {
        JMethod method = csMethod.getMethod();
        if (relevantMethods.contains(method)) {
            method.getIR()
                    .invokes(false)
                    .forEach(invoke -> {
                        handleForName(csMethod, invoke);
                        passTarget(classTargets, csMethod, invoke, BASE);
                        passTarget(memberTargets, csMethod, invoke, BASE);
                        passTarget(arrayTypeTargets, csMethod, invoke, 0);
                    });
        }
    }

    private void handleForName(CSMethod csMethod, Invoke invoke) {
        if (forNameTargets.containsKey(invoke)) {
            Context context = csMethod.getContext();
            Var result = invoke.getResult();
            forNameTargets.get(invoke).forEach(target -> {
                solver.initializeClass(target);
                if (result != null) {
                    solver.addVarPointsTo(context, result, helper.getLogMetaObj(target));
                }
            });
        }
    }

    private <T> void passTarget(MultiMap<Invoke, T> targetMap,
                                CSMethod csMethod, Invoke invoke, int index) {
        if (targetMap.containsKey(invoke)) {
            Context context = csMethod.getContext();
            Var var = InvokeUtils.getVar(invoke, index);
            targetMap.get(invoke).forEach(target ->
                    solver.addVarPointsTo(context, var, helper.getLogMetaObj(target)));
        }
    }
}
