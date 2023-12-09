

package keeno.usap.analysis.pta.plugin.natives;

import keeno.usap.analysis.pta.core.solver.Solver;
import keeno.usap.analysis.pta.plugin.util.AbstractIRModel;
import keeno.usap.analysis.pta.plugin.util.InvokeHandler;
import keeno.usap.ir.exp.ArrayAccess;
import keeno.usap.ir.exp.InstanceFieldAccess;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.ir.stmt.StoreArray;
import keeno.usap.ir.stmt.StoreField;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.type.ArrayType;
import keeno.usap.language.type.ClassType;
import keeno.usap.language.type.PrimitiveType;
import keeno.usap.language.type.ReferenceType;
import keeno.usap.language.type.Type;

import java.util.ArrayList;
import java.util.List;

public class UnsafeModel extends AbstractIRModel {

    private int counter = 0;

    UnsafeModel(Solver solver) {
        super(solver);
    }

    @InvokeHandler(signature = "<sun.misc.Unsafe: boolean compareAndSwapObject(java.lang.Object,long,java.lang.Object,java.lang.Object)>")
    public List<Stmt> compareAndSwapObject(Invoke invoke) {
        // unsafe.compareAndSwapObject(o, offset, expected, x);
        List<Var> args = invoke.getInvokeExp().getArgs();
        List<Stmt> stmts = new ArrayList<>();
        Var o = args.get(0);
        Var x = args.get(3);
        if (o.getType() instanceof ArrayType) { // if o is of ArrayType
            // generate o[i] = x;
            Var i = new Var(invoke.getContainer(),
                    "%unsafe-index" + counter++, PrimitiveType.INT, -1);
            stmts.add(new StoreArray(new ArrayAccess(o, i), x));
        } else { // otherwise, o is of ClassType
            // generate o.f = x; for field f that has the same type of x.
            JClass clazz = ((ClassType) o.getType()).getJClass();
            Type xType = x.getType();
            if (xType instanceof ReferenceType) { // ignore primitive types
                clazz.getDeclaredFields()
                        .stream()
                        .filter(f -> f.getType().equals(xType))
                        .forEach(f -> stmts.add(new StoreField(
                                new InstanceFieldAccess(f.getRef(), o), x)));
            }
        }
        return stmts;
    }
}
