

package keeno.usap.analysis.pta.plugin.natives;

import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.AbstractIRModel;
import keeno.usap.analysis.pta.plugin.util.InvokeHandler;
import keeno.usap.ir.exp.ArrayAccess;
import keeno.usap.ir.exp.CastExp;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Cast;
import keeno.usap.ir.stmt.Copy;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.LoadArray;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.StoreArray;
import keeno.usap.language.classes.ClassNames;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.Type;

import java.util.ArrayList;
import java.util.List;

public class ArrayModel extends AbstractIRModel {

    private final ClassType objType;

    private final ArrayType objArrayType;

    /**
     * Counter for naming temporary variables.
     */
    private int counter = 0;

    ArrayModel(Solver solver) {
        super(solver);
        objType = typeSystem.getClassType(ClassNames.OBJECT);
        objArrayType = typeSystem.getArrayType(objType, 1);
    }

    @InvokeHandler(signature = "<java.util.Arrays: java.lang.Object[] copyOf(java.lang.Object[],int)>")
    public List<Stmt> arraysCopyOf(Invoke invoke) {
        Var result = invoke.getResult();
        return result != null
                ? List.of(new Copy(result, invoke.getInvokeExp().getArg(0)))
                : List.of();
    }

    @InvokeHandler(signature = "<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>")
    public List<Stmt> systemArraycopy(Invoke invoke) {
        JMethod container = invoke.getContainer();
        Var src = getTempVar(container, "src", objArrayType);
        Var dest = getTempVar(container, "dest", objArrayType);
        Var temp = getTempVar(container, "temp", objType);
        List<Stmt> stmts = new ArrayList<>();
        List<Var> args = invoke.getInvokeExp().getArgs();
        stmts.add(new Cast(src, new CastExp(args.get(0), objArrayType)));
        stmts.add(new Cast(dest, new CastExp(args.get(2), objArrayType)));
        stmts.add(new LoadArray(temp, new ArrayAccess(src, args.get(1))));
        stmts.add(new StoreArray(new ArrayAccess(dest, args.get(3)), temp));
        return stmts;
    }

    private Var getTempVar(JMethod container, String name, Type type) {
        String varName = "%native-arraycopy-" + name + counter++;
        return new Var(container, varName, type, -1);
    }
}
