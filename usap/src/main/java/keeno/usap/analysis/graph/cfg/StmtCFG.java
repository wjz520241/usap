

package keeno.usap.analysis.graph.cfg;

import keeno.usap.ir.IR;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.util.collection.Sets;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

/**
 * CFG with {@code Stmt} as nodes. This class maintains a mapping between
 * indexes and nodes in this graph as follows:
 * <ul>
 *     <li>Entry node is mapped to index 0</li>
 *     <li>Exit node is mapped to index <code>ir.getStmts().size() + 1</code></li>
 *     <li>Other nodes (stmts) are mapped to index <code>stmt.getIndex() + 1</code></li>
 * </ul>
 * Basically, it vacates index 0 for entry node, shifts stmts in IR by 1,
 * and appends exit node at last.
 */
class StmtCFG extends AbstractCFG<Stmt> {

    public StmtCFG(IR ir) {
        super(ir);
    }

    @Override
    public int getIndex(Stmt stmt) {
        if (isEntry(stmt)) {
            return 0;
        } else if (isExit(stmt)) {
            return ir.getStmts().size() + 1;
        } else {
            return stmt.getIndex() + 1;
        }
    }

    @Override
    public Stmt getNode(int index) {
        if (index == 0) {
            return getEntry();
        } else if (index == ir.getStmts().size() + 1) {
            return getExit();
        } else {
            return ir.getStmt(index - 1);
        }
    }

    @Override
    public Set<Stmt> getNodes() {
        // keep nodes sorted to ease debugging
        Set<Stmt> stmts = Sets.newOrderedSet(Comparator.comparing(this::getIndex));
        stmts.addAll(super.getNodes());
        return Collections.unmodifiableSet(stmts);
    }
}
