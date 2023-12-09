

package keeno.usap.analysis.dataflow.analysis.constprop;

import keeno.usap.ir.exp.ArithmeticExp;
import keeno.usap.ir.exp.BinaryExp;
import keeno.usap.ir.exp.BitwiseExp;
import keeno.usap.ir.exp.ConditionExp;
import keeno.usap.ir.exp.Exp;
import keeno.usap.ir.exp.Exps;
import keeno.usap.ir.exp.IntLiteral;
import keeno.usap.ir.exp.ShiftExp;
import keeno.usap.ir.exp.Var;
import keeno.usap.util.AnalysisException;

/**
 * Evaluates expressions in constant propagation. Since this functionality
 * is used not only by {@link ConstantPropagation} but also other classes,
 * we implement it as static methods to make it easily accessible.
 */
public final class Evaluator {

    private Evaluator() {
    }

    /**
     * Evaluates the {@link Value} of given expression.
     *
     * @param exp the expression to be evaluated
     * @param in  IN fact of the statement
     * @return the resulting {@link Value}
     */
    public static Value evaluate(Exp exp, CPFact in) {
        if (exp instanceof IntLiteral) {
            return Value.makeConstant(((IntLiteral) exp).getValue());
        } else if (exp instanceof Var var) {
            // treat the values of non-int variables as NAC
            return Exps.holdsInt(var) ? in.get(var) : Value.getNAC();
        } else if (exp instanceof BinaryExp binary) {
            BinaryExp.Op op = binary.getOperator();
            Value v1 = evaluate(binary.getOperand1(), in);
            Value v2 = evaluate(binary.getOperand2(), in);
            // handle division-by-zero by returning UNDEF
            if ((op == ArithmeticExp.Op.DIV || op == ArithmeticExp.Op.REM) &&
                    v2.isConstant() && v2.getConstant() == 0) {
                return Value.getUndef();
            }
            if (v1.isConstant() && v2.isConstant()) {
                int i1 = v1.getConstant();
                int i2 = v2.getConstant();
                return Value.makeConstant(evaluate(op, i1, i2));
            }
            // handle zero * NAC by returning 0
            if (op == ArithmeticExp.Op.MUL
                    && (v1.isConstant() && v1.getConstant() == 0 && v2.isNAC() || // 0 * NAC
                    v2.isConstant() && v2.getConstant() == 0 && v1.isNAC())) { // NAC * 0
                return Value.makeConstant(0);
            }
            if (v1.isNAC() || v2.isNAC()) {
                return Value.getNAC();
            }
            return Value.getUndef();
        }
        // return NAC for other cases
        return Value.getNAC();
    }

    private static int evaluate(BinaryExp.Op op, int i1, int i2) {
        if (op instanceof ArithmeticExp.Op) {
            return switch ((ArithmeticExp.Op) op) {
                case ADD -> i1 + i2;
                case SUB -> i1 - i2;
                case MUL -> i1 * i2;
                case DIV -> i1 / i2;
                case REM -> i1 % i2;
            };
        } else if (op instanceof BitwiseExp.Op) {
            return switch ((BitwiseExp.Op) op) {
                case OR -> i1 | i2;
                case AND -> i1 & i2;
                case XOR -> i1 ^ i2;
            };
        } else if (op instanceof ConditionExp.Op) {
            return switch ((ConditionExp.Op) op) {
                case EQ -> i1 == i2 ? 1 : 0;
                case NE -> i1 != i2 ? 1 : 0;
                case LT -> i1 < i2 ? 1 : 0;
                case GT -> i1 > i2 ? 1 : 0;
                case LE -> i1 <= i2 ? 1 : 0;
                case GE -> i1 >= i2 ? 1 : 0;
            };
        } else if (op instanceof ShiftExp.Op) {
            return switch ((ShiftExp.Op) op) {
                case SHL -> i1 << i2;
                case SHR -> i1 >> i2;
                case USHR -> i1 >>> i2;
            };
        }
        throw new AnalysisException("Unexpected op: " + op);
    }
}
