

package keeno.usap.analysis.pta.core.heap;

import keeno.usap.World;
import keeno.usap.config.AnalysisOptions;
import keeno.usap.config.ConfigException;
import keeno.usap.ir.exp.ReferenceLiteral;
import keeno.usap.ir.exp.StringLiteral;
import keeno.usap.ir.stmt.New;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;
import keeno.usap.language.type.TypeSystem;
import keeno.usap.util.Predicates;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.TwoKeyMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static keeno.usap.language.classes.ClassNames.STRING;
import static keeno.usap.language.classes.ClassNames.STRING_BUFFER;
import static keeno.usap.language.classes.ClassNames.STRING_BUILDER;
import static keeno.usap.language.classes.ClassNames.THROWABLE;

/**
 * All heap models should inherit this class, and we can define
 * some uniform behaviors of heap modeling here.
 */
public abstract class AbstractHeapModel implements HeapModel {

    /**
     * Predicate used to check whether a given string constant
     * in the program should be distinguished.
     */
    private final Predicate<String> isDistinguishedSC;

    private final boolean isMergeStringObjects;

    private final boolean isMergeStringBuilders;

    private final boolean isMergeExceptionObjects;

    private final TypeSystem typeSystem;

    private final ClassType string;

    private final ClassType stringBuilder;

    private final ClassType stringBuffer;

    private final ClassType throwable;

    private final Map<New, NewObj> newObjs = Maps.newMap();

    private final TwoKeyMap<Type, ReferenceLiteral, ConstantObj> constantObjs
            = Maps.newTwoKeyMap();

    /**
     * The merged object representing string constants.
     */
    private final MergedObj mergedSC;

    private final Map<Type, MergedObj> mergedObjs = Maps.newMap();

    private final Map<MockObj, MockObj> mockObjs = Maps.newMap();

    /**
     * Counter for indexing Objs.
     */
    private int counter = 0;

    private final List<Obj> objs = new ArrayList<>(1024);

    protected AbstractHeapModel(AnalysisOptions options) {
        isDistinguishedSC = getSCPredicate(
                options.getString("distinguish-string-constants"));
        isMergeStringObjects = options.getBoolean("merge-string-objects");
        isMergeStringBuilders = options.getBoolean("merge-string-builders");
        isMergeExceptionObjects = options.getBoolean("merge-exception-objects");
        typeSystem = World.get().getTypeSystem();
        string = typeSystem.getClassType(STRING);
        stringBuilder = typeSystem.getClassType(STRING_BUILDER);
        stringBuffer = typeSystem.getClassType(STRING_BUFFER);
        throwable = typeSystem.getClassType(THROWABLE);
        mergedSC = add(new MergedObj(string, "<Merged string constants>"));
    }

    @SuppressWarnings("unchecked")
    private static Predicate<String> getSCPredicate(String option) {
        if (option == null) {
            return Predicates.alwaysFalse();
        } else {
            return switch (option) {
                case "reflection" -> new IsReflectionString();
                case "app" -> new IsApplicationString();
                case "all" -> Predicates.alwaysTrue();
                default -> { // in this case, we assume that 'option' is name
                    // of the predicate class
                    try {
                        Class<?> clazz = Class.forName(option);
                        Constructor<?> ctor = clazz.getConstructor();
                        yield (Predicate<String>) ctor.newInstance();
                    } catch (ClassNotFoundException | NoSuchMethodException |
                             InvocationTargetException | InstantiationException |
                             IllegalAccessException e) {
                        throw new ConfigException("Failed to initialize custom predicate "
                                + option + " for distinguishing string constants", e);
                    }
                }
            };
        }
    }

    @Override
    public Obj getObj(New allocSite) {
        Type type = allocSite.getRValue().getType();
        if (isMergeStringObjects && type.equals(string)) {
            return getMergedObj(allocSite);
        }
        if (isMergeStringBuilders &&
                (type.equals(stringBuilder) || type.equals(stringBuffer))) {
            return getMergedObj(allocSite);
        }
        if (isMergeExceptionObjects && typeSystem.isSubtype(throwable, type)) {
            return getMergedObj(allocSite);
        }
        return doGetObj(allocSite);
    }

    /**
     * Merges given object given by its type.
     *
     * @param allocSite the allocation site of the object
     * @return the merged object
     */
    protected MergedObj getMergedObj(New allocSite) {
        MergedObj mergedObj = mergedObjs.computeIfAbsent(
                allocSite.getRValue().getType(),
                t -> add(new MergedObj(t, "<Merged " + t + ">")));
        mergedObj.addRepresentedObj(getNewObj(allocSite));
        return mergedObj;
    }

    protected NewObj getNewObj(New allocSite) {
        return newObjs.computeIfAbsent(allocSite,
                site -> add(new NewObj(site)));
    }

    /**
     * The method which controls the heap modeling for normal objects.
     */
    protected abstract Obj doGetObj(New allocSite);

    @Override
    public Obj getConstantObj(ReferenceLiteral value) {
        Obj obj = doGetConstantObj(value);
        if (value instanceof StringLiteral stringLiteral
                && !isDistinguishedSC.test(stringLiteral.getString())) {
            mergedSC.addRepresentedObj(obj);
            return mergedSC;
        }
        return obj;
    }

    protected Obj doGetConstantObj(ReferenceLiteral value) {
        return constantObjs.computeIfAbsent(value.getType(), value,
                (t, v) -> add(new ConstantObj(v)));
    }

    @Override
    public boolean isStringConstant(Obj obj) {
        return obj.getAllocation() instanceof StringLiteral ||
                obj.equals(mergedSC);
    }

    @Override
    public Obj getMockObj(Descriptor desc, Object alloc, Type type,
                          JMethod container, boolean isFunctional) {
        MockObj mockObj = new MockObj(desc, alloc, type, container, isFunctional);
        return mockObjs.computeIfAbsent(mockObj, this::add);
    }

    /**
     * Adds an obj to this model. This method also sets index for given obj.
     * Each obj should be passed to this method only once.
     */
    protected <T extends Obj> T add(T obj) {
        objs.add(obj);
        obj.setIndex(counter++);
        return obj;
    }

    @Override
    public Collection<Obj> getObjects() {
        return Collections.unmodifiableList(objs);
    }

    @Override
    public int getIndex(Obj o) {
        return o.getIndex();
    }

    @Override
    public Obj getObject(int index) {
        return objs.get(index);
    }
}
