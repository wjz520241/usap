

package keeno.usap.analysis.pta.plugin.invokedynamic;

import keeno.usap.analysis.pta.core.cs.context.Context;
import keeno.usap.analysis.pta.core.cs.element.CSVar;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.AbstractModel;
import keeno.usap.analysis.pta.plugin.util.CSObjs;
import keeno.usap.analysis.pta.plugin.util.InvokeHandler;
import keeno.usap.analysis.pta.pts.PointsToSet;
import keeno.usap.ir.exp.MethodType;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.language.type.Type;

import java.util.List;

/**
 * Models invocations to MethodType.methodType(*);
 */
public class MethodTypeModel extends AbstractModel {

    MethodTypeModel(Solver solver) {
        super(solver);
    }

    @InvokeHandler(signature = "<java.lang.invoke.MethodType: java.lang.invoke.MethodType methodType(java.lang.Class)>", argIndexes = {0})
    public void methodType1Class(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            Context context = csVar.getContext();
            pts.forEach(obj -> {
                Type retType = CSObjs.toType(obj);
                if (retType != null) {
                    MethodType mt = MethodType.get(List.of(), retType);
                    Obj mtObj = heapModel.getConstantObj(mt);
                    solver.addVarPointsTo(context, result, mtObj);
                }
            });
        }
    }

    @InvokeHandler(signature = "<java.lang.invoke.MethodType: java.lang.invoke.MethodType methodType(java.lang.Class,java.lang.Class)>", argIndexes = {0, 1})
    public void methodType2Classes(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            List<PointsToSet> args = getArgs(csVar, pts, invoke, 0, 1);
            PointsToSet retObjs = args.get(0);
            PointsToSet paramObjs = args.get(1);
            Context context = csVar.getContext();
            retObjs.forEach(retObj -> {
                Type retType = CSObjs.toType(retObj);
                if (retType != null) {
                    paramObjs.forEach(paramObj -> {
                        Type paramType = CSObjs.toType(paramObj);
                        if (paramType != null) {
                            MethodType mt = MethodType.get(List.of(paramType), retType);
                            Obj mtObj = heapModel.getConstantObj(mt);
                            solver.addVarPointsTo(context, result, mtObj);
                        }
                    });
                }
            });
        }
    }

    @InvokeHandler(signature = "<java.lang.invoke.MethodType: java.lang.invoke.MethodType methodType(java.lang.Class,java.lang.invoke.MethodType)>", argIndexes = {0, 1})
    public void methodTypeClassMT(CSVar csVar, PointsToSet pts, Invoke invoke) {
        Var result = invoke.getResult();
        if (result != null) {
            List<PointsToSet> args = getArgs(csVar, pts, invoke, 0, 1);
            PointsToSet retObjs = args.get(0);
            PointsToSet mtObjs = args.get(1);
            Context context = csVar.getContext();
            retObjs.forEach(retObj -> {
                Type retType = CSObjs.toType(retObj);
                if (retType != null) {
                    mtObjs.forEach(mtObj -> {
                        MethodType mt = CSObjs.toMethodType(mtObj);
                        if (mt != null) {
                            MethodType resultMT = MethodType.get(mt.getParamTypes(), retType);
                            Obj resultMTObj = heapModel.getConstantObj(resultMT);
                            solver.addVarPointsTo(context, result, resultMTObj);
                        }
                    });
                }
            });
        }
    }
}
