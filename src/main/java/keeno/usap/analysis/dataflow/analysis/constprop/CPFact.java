

package keeno.usap.analysis.dataflow.analysis.constprop;

import keeno.usap.analysis.dataflow.fact.MapFact;
import keeno.usap.ir.exp.IntLiteral;
import keeno.usap.ir.exp.Var;

import java.util.Collections;
import java.util.Map;

/**
 * Represents data facts of constant propagation, which maps variables
 * to their lattice values.
 * <p>
 * For better performance, this implementation treats temporary constant
 * variables specially. These variables have two features:
 * <ul>
 *     <li>its value is associated with itself</li>
 *     <li>its value never change</li>
 * </ul>
 * So this map does not need to actually store the values of these variables:
 * the values must be constants and can be obtained from the variables themselves.
 * <p>
 * Note that in this implementation, we use absence to represent UNDEF,
 * i.e., if a CPFact does not contain variable-value mapping of a variable,
 * it represents that the lattice value of the variable is UNDEF;
 * moreover, if we set the lattice value of a variable to UNDEF,
 * it effectively removes the variable from the CPFact.
 */
public class CPFact extends MapFact<Var, Value> {

    public CPFact() {
        this(Collections.emptyMap());
    }

    private CPFact(Map<Var, Value> map) {
        super(map);
    }

    /**
     * @return the value of given variable in this fact,
     * or UNDEF the variable is absent in this fact.
     */
    @Override
    public Value get(Var var) {
        if (var.isConst() &&
                var.getConstValue() instanceof IntLiteral i) {
            // for temporary constant variable, directly return
            // the associated value
            return Value.makeConstant(i.getValue());
        } else {
            return map.getOrDefault(var, Value.getUndef());
        }
    }

    @Override
    public boolean update(Var var, Value value) {
        if (var.isConst()) {
            // do not store temporary constant variables
            return false;
        } else if (value.isUndef()) {
            // if the client code sets variable key to UNDEF,
            // then we remove the variable from the CPFact
            // as we use absence to represent UNDEF.
            return remove(var) != null;
        } else {
            return super.update(var, value);
        }
    }

    @Override
    public CPFact copy() {
        return new CPFact(this.map);
    }
}
