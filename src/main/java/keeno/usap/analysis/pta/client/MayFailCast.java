

package keeno.usap.analysis.pta.client;

import keeno.usap.World;
import keeno.usap.analysis.pta.PointerAnalysisResult;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.ir.exp.Var;
import keeno.usap.ir.stmt.Cast;
import keeno.usap.ir.stmt.Stmt;
import keeno.usap.language.type.Type;

public class MayFailCast extends Collector {

    public static final String ID = "may-fail-cast";

    public MayFailCast(AnalysisConfig config) {
        super(config);
    }

    @Override
    boolean isRelevant(Stmt stmt) {
        return stmt instanceof Cast;
    }

    @Override
    boolean isWanted(Stmt stmt, PointerAnalysisResult result) {
        Cast cast = (Cast) stmt;
        Type castType = cast.getRValue().getCastType();
        Var from = cast.getRValue().getValue();
        for (Obj obj : result.getPointsToSet(from)) {
            if (!World.get().getTypeSystem().isSubtype(
                    castType, obj.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    String getDescription() {
        return ID;
    }
}
