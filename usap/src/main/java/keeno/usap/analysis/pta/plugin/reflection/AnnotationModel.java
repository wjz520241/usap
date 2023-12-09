

package keeno.usap.analysis.pta.plugin.reflection;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.CSObjs;
import keeno.usap.analysis.pta.plugin.util.SolverHolder;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.proginfo.MethodRef;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.annotation.Annotation;
import keeno.usap.language.annotation.ClassElement;
import keeno.usap.language.annotation.Element;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.Subsignature;

/**
 * Handles annotation-related APIs.
 * Since usually these APIs are tightly coupled with reflection and use
 * reflection objects, we put this class in this package.
 */
class AnnotationModel extends SolverHolder {

    private final MetaObjHelper helper;

    private final JClass annotation;

    private final Subsignature annotationType;

    AnnotationModel(Solver solver, MetaObjHelper helper) {
        super(solver);
        this.helper = helper;
        this.annotation = hierarchy.getClass(ClassNames.ANNOTATION);
        this.annotationType = Subsignature.get("java.lang.Class annotationType()");
    }

    /**
     * Annotation objects are of interface types, thus the calls on them
     * can not be resolved, and we handle such calls here.
     */
    void onUnresolvedCall(CSObj recv, Context context, Invoke invoke) {
        MethodRef ref = invoke.getMethodRef();
        JClass declaringClass = ref.getDeclaringClass();
        if (declaringClass.equals(annotation) &&
                ref.getSubsignature().equals(annotationType)) {
            annotationType(recv, context, invoke);
        }
        if (hierarchy.isSubclass(annotation, declaringClass)) {
            getElement(recv, context, invoke);
        }
    }

    private void annotationType(CSObj recv, Context context, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            Annotation a = CSObjs.toAnnotation(recv);
            if (a != null) {
                JClass annoType = hierarchy.getClass(a.getType());
                Obj annoTypeObj = helper.getMetaObj(annoType);
                solver.addVarPointsTo(context, result, annoTypeObj);
            }
        }
    }

    /**
     * Models APIs to obtain annotation elements.
     */
    private void getElement(CSObj recv, Context context, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            Annotation a = CSObjs.toAnnotation(recv);
            if (a != null) {
                String methodName = invoke.getMethodRef().getName();
                Element elem = a.getElement(methodName);
                if (elem instanceof ClassElement classElem) {
                    JClass clazz = hierarchy.getClass(classElem.classDescriptor());
                    Obj classObj = helper.getMetaObj(clazz);
                    solver.addVarPointsTo(context, result, classObj);
                }
            }
        }
    }
}
