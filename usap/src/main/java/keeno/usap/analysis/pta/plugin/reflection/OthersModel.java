

package keeno.usap.analysis.pta.plugin.reflection;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.heap.Descriptor;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.AbstractModel;
import keeno.usap.analysis.pta.plugin.util.CSObjs;
import keeno.usap.analysis.pta.plugin.util.InvokeHandler;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.ClassLiteral;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.annotation.Annotation;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.Type;
import keeno.usap.language.type.VoidType;

import java.util.ArrayList;
import java.util.List;

import static keeno.usap.analysis.pta.plugin.util.InvokeUtils.BASE;

/**
 * Models other non-core reflection APIs.
 */
public class OthersModel extends AbstractModel {

    private static final Descriptor PARAM_ANNOTATIONS = () -> "ParamAnnotations";

    private final MetaObjHelper helper;

    /**
     * Annotation[].
     */
    private final ArrayType annotation1Array;

    /**
     * Annotation[][].
     */
    private final ArrayType annotation2Array;

    public OthersModel(Solver solver, MetaObjHelper helper) {
        super(solver);
        this.helper = helper;
        ClassType annotationType = typeSystem.getClassType(ClassNames.ANNOTATION);
        annotation1Array = typeSystem.getArrayType(annotationType, 1);
        annotation2Array = typeSystem.getArrayType(annotationType, 2);
    }

    // ---------- Model for java.lang.Object starts ----------
    @InvokeHandler(signature = "<java.lang.Object: java.lang.Class getClass()>", argIndexes = {BASE})
    public void getClass(CSVar csVar, PointsToSet pts, Invoke invoke) {
        if (!invoke.getContainer().isApplication()) {
            // ignore Object.getClass() in library code, until
            // we have better treatment with relevant reflective calls
            return;
        }
        Var result = invoke.getResult();
        if (result != null) {
            Context context = csVar.getContext();
            pts.forEach(recv -> {
                Type type = recv.getObject().getType();
                if (type instanceof ClassType classType) {
                    Obj classObj = helper.getMetaObj(classType.getJClass());
                    solver.addVarPointsTo(context, result, classObj);
                }
            });
        }
    }
    // ---------- Model for java.lang.Object ends ----------

    // ---------- Model for java.lang.Class starts ----------
    @InvokeHandler(signature = "<java.lang.Class: java.lang.Class getPrimitiveClass(java.lang.String)>", argIndexes = {0})
    public void getPrimitiveClass(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            pts.forEach(nameObj -> {
                String name = CSObjs.toString(nameObj);
                if (name != null) {
                    Type type = name.equals("void") ?
                            VoidType.VOID : PrimitiveType.get(name);
                    solver.addVarPointsTo(csVar.getContext(), result,
                            heapModel.getConstantObj(ClassLiteral.get(type)));
                }
            });
        }
    }

    @InvokeHandler(signature = "<java.lang.Class: java.lang.annotation.Annotation getAnnotation(java.lang.Class)>", argIndexes = {BASE, 0})
    public void getAnnotation(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            List<PointsToSet> args = getArgs(csVar, pts, invoke, BASE, 0);
            PointsToSet baseClasses = args.get(0);
            PointsToSet annoClasses = args.get(1);
            Context context = csVar.getContext();
            baseClasses.forEach(baseClass -> {
                JClass baseClazz = CSObjs.toClass(baseClass);
                if (baseClazz != null) {
                    annoClasses.forEach(annoClass -> {
                        JClass annoClazz = CSObjs.toClass(annoClass);
                        if (annoClazz != null) {
                            Annotation a = baseClazz.getAnnotation(annoClazz.getName());
                            if (a != null) {
                                Obj annoObj = helper.getAnnotationObj(a);
                                solver.addVarPointsTo(context, result, annoObj);
                            }
                        }
                    });
                }
            });
        }
    }
    // ---------- Model for java.lang.Class ends ----------

    // ---------- Model for java.lang.reflect.Method starts ----------
    @InvokeHandler(signature = "<java.lang.reflect.Method: java.lang.annotation.Annotation[][] getParameterAnnotations()>", argIndexes = {BASE})
    public void getParameterAnnotations(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            pts.forEach(mtdObj -> {
                JMethod method = CSObjs.toMethod(mtdObj);
                if (method != null) {
                    List<Annotation> paramAnnos = new ArrayList<>();
                    for (int i = 0; i < method.getParamCount(); ++i) {
                        paramAnnos.addAll(method.getParamAnnotations(i));
                    }
                    if (!paramAnnos.isEmpty()) {
                        Context context = csVar.getContext();
                        PointsToSet annoSet = solver.makePointsToSet();
                        paramAnnos.forEach(anno -> {
                            Obj annoObj = helper.getAnnotationObj(anno);
                            annoSet.addObject(csManager.getCSObj(emptyContext, annoObj));
                        });
                        // Annotation[*] anno1Array = annotation;
                        Obj anno1Array = heapModel.getMockObj(PARAM_ANNOTATIONS, invoke,
                                annotation1Array, invoke.getContainer());
                        CSObj csAnno1Array = csManager.getCSObj(context, anno1Array);
                        solver.addPointsTo(csManager.getArrayIndex(csAnno1Array), annoSet);
                        // Annotation[*][] anno2Array = anno1Array;
                        Obj anno2Array = heapModel.getMockObj(PARAM_ANNOTATIONS, invoke,
                                annotation2Array, invoke.getContainer());
                        CSObj csAnno2Array = csManager.getCSObj(context, anno2Array);
                        solver.addPointsTo(csManager.getArrayIndex(csAnno2Array), csAnno1Array);
                        // result = anno2Array;
                        solver.addVarPointsTo(context, result, anno2Array);
                    }
                }
            });
        }
    }
    // ---------- Model for java.lang.reflect.Method ends ----------
}
