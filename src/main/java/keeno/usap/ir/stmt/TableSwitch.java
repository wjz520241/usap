

package keeno.usap.ir.stmt;

import keeno.usap.ir.exp.Var;
import keeno.usap.util.collection.Pair;

import java.util.List;
import java.util.stream.IntStream;

public class TableSwitch extends SwitchStmt {

    private final int lowIndex;

    private final int highIndex;

    public TableSwitch(Var var, int lowIndex, int highIndex) {
        super(var);
        this.lowIndex = lowIndex;
        this.highIndex = highIndex;
    }

    public int getLowIndex() {
        return lowIndex;
    }

    public int getHighIndex() {
        return highIndex;
    }

    @Override
    public List<Integer> getCaseValues() {
        return IntStream.range(lowIndex, highIndex + 1)
                .boxed()
                .toList();
    }

    @Override
    public List<Pair<Integer, Stmt>> getCaseTargets() {
        return IntStream.range(lowIndex, highIndex + 1)
                .mapToObj(i -> new Pair<>(i,
                        targets == null ? null : targets.get(i - lowIndex)))
                .toList();
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getInsnString() {
        return "tableswitch";
    }
}
