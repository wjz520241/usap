

package keeno.usap.analysis.defuse;

import keeno.usap.analysis.StmtResult;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.TwoKeyMultiMap;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the analysis result of {@link DefUseAnalysis}, i.e.,
 * both def-use chain and use-def chain.
 */
public class DefUse implements StmtResult<MultiMap<Var, Stmt>> {

    private static final String NULL_DEFS = "defs is null (not computed)" +
            " as it is disabled in def-use analysis";

    private static final String NULL_USES = "uses is null (not computed)" +
            " as it is disabled in def-use analysis";

    @Nullable
    private final TwoKeyMultiMap<Stmt, Var, Stmt> defs;

    @Nullable
    private final MultiMap<Stmt, Stmt> uses;

    DefUse(@Nullable TwoKeyMultiMap<Stmt, Var, Stmt> defs,
           @Nullable MultiMap<Stmt, Stmt> uses) {
        this.defs = defs;
        this.uses = uses;
    }

    /**
     * @return definitions of {@code var} at {@code stmt}.
     * If {@code var} is not used in {@code stmt} or it does not
     * have any definitions, an empty set is returned.
     */
    public Set<Stmt> getDefs(Stmt stmt, Var var) {
        Objects.requireNonNull(defs, NULL_DEFS);
        return defs.get(stmt, var);
    }

    /**
     * @return uses of the variable defined by {@code stmt}.
     * If {@code stmt} does not define any variable or the defined variable
     * does not have any uses, an empty Set is returned.
     */
    public Set<Stmt> getUses(Stmt stmt) {
        Objects.requireNonNull(uses, NULL_USES);
        return uses.get(stmt);
    }

    @Override
    public boolean isRelevant(Stmt stmt) {
        return true;
    }

    /**
     * {@link StmtResult} for def-use analysis. Note that this result only
     * contains use-def chain, and it is mainly for testing purpose.
     */
    @Override
    public MultiMap<Var, Stmt> getResult(Stmt stmt) {
        Objects.requireNonNull(defs, NULL_DEFS);
        return defs.get(stmt);
    }
}
