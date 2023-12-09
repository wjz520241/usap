

package keeno.usap.analysis.pta.plugin.reflection;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.CSObjs;
import keeno.usap.analysis.pta.plugin.util.InvokeHandler;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.classes.JClass;

import java.util.List;
import java.util.Set;

import static keeno.usap.analysis.pta.plugin.util.InvokeUtils.BASE;

public class StringBasedModel extends InferenceModel {

    StringBasedModel(Solver solver, MetaObjHelper helper, Set<Invoke> invokesWithLog) {
        super(solver, helper, invokesWithLog);
    }

    @Override
    protected void handleNewNonInvokeStmt(Stmt stmt) {
        // nothing to do
    }

    @InvokeHandler(signature = "<java.lang.Class: java.lang.Class forName(java.lang.String)>", argIndexes = {0})
    @InvokeHandler(signature = "<java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>", argIndexes = {0})
    public void classForName(CSVar csVar, PointsToSet pts, Invoke invoke) {
        if (invokesWithLog.contains(invoke)) {
            return;
        }
        Context context = csVar.getContext();
        pts.forEach(obj -> classForNameKnown(context, invoke, CSObjs.toString(obj)));
    }

    @InvokeHandler(signature = "<java.lang.Class: java.lang.reflect.Constructor getConstructor(java.lang.Class[])>", argIndexes = {BASE})
    @InvokeHandler(signature = "<java.lang.Class: java.lang.reflect.Constructor getDeclaredConstructor(java.lang.Class[])>", argIndexes = {BASE})
    public void classGetConstructor(CSVar csVar, PointsToSet pts, Invoke invoke) {
        if (invokesWithLog.contains(invoke)) {
            return;
        }
        Context context = csVar.getContext();
        pts.forEach(obj -> classGetConstructorKnown(context, invoke, CSObjs.toClass(obj)));
    }

    @InvokeHandler(signature = "<java.lang.Class: java.lang.reflect.Method getMethod(java.lang.String,java.lang.Class[])>", argIndexes = {BASE, 0})
    @InvokeHandler(signature = "<java.lang.Class: java.lang.reflect.Method getDeclaredMethod(java.lang.String,java.lang.Class[])>", argIndexes = {BASE, 0})
    public void classGetMethod(CSVar csVar, PointsToSet pts, Invoke invoke) {
        if (invokesWithLog.contains(invoke)) {
            return;
        }
        List<PointsToSet> args = getArgs(csVar, pts, invoke, BASE, 0);
        PointsToSet classObjs = args.get(0);
        PointsToSet nameObjs = args.get(1);
        Context context = csVar.getContext();
        classObjs.forEach(classObj -> {
            JClass clazz = CSObjs.toClass(classObj);
            nameObjs.forEach(nameObj -> {
                String name = CSObjs.toString(nameObj);
                classGetMethodKnown(context, invoke, clazz, name);
            });
        });
    }
}
