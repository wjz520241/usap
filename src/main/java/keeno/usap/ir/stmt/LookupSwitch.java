

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.Var;
import keeno.usap.util.collection.Pair;

import java.util.List;
import java.util.stream.IntStream;

public class LookupSwitch extends SwitchStmt {

    private final List<Integer> caseValues;

    public LookupSwitch(Var var, List<Integer> caseValues) {
        super(var);
        this.caseValues = List.copyOf(caseValues);
    }

    public int getCaseValue(int index) {
        return caseValues.get(index);
    }

    @Override
    public List<Integer> getCaseValues() {
        return caseValues;
    }

    @Override
    public List<Pair<Integer, Stmt>> getCaseTargets() {
        return IntStream.range(0, caseValues.size())
                .mapToObj(i -> new Pair<>(caseValues.get(i),
                        targets == null ? null : targets.get(i)))
                .toList();
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getInsnString() {
        return "lookupswitch";
    }
}
