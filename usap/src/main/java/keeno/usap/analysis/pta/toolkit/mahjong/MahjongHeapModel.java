

package keeno.usap.analysis.pta.toolkit.mahjong;

import keeno.usap.analysis.pta.core.heap.AbstractHeapModel;
import keeno.usap.analysis.pta.core.heap.MergedObj;
import keeno.usap.analysis.pta.core.heap.NewObj;
import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.config.AnalysisOptions;
import keeno.usap.ir.stmt.New;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.CollectionUtils;
import keeno.usap.util.collection.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class MahjongHeapModel extends AbstractHeapModel {

    // currently, perform merging for only NewObj
    private final Map<New, MergedObj> mergeMap;

    MahjongHeapModel(AnalysisOptions options, Collection<Set<Obj>> objGroups) {
        super(options);
        mergeMap = buildMergeMap(objGroups);
    }

    private Map<New, MergedObj> buildMergeMap(Collection<Set<Obj>> objGroups) {
        Map<New, MergedObj> mergeMap = Maps.newMap();
        objGroups.stream()
                .filter(objs -> objs.size() > 1)
                .forEach(objs -> {
                    Type type = CollectionUtils.getOne(objs).getType();
                    MergedObj mergedObj = add(new MergedObj(type,
                            "<Mahjong-merged " + type + ">"));
                    objs.forEach(obj -> {
                        if (obj instanceof NewObj newObj) {
                            New allocSite = newObj.getAllocation();
                            mergeMap.put(allocSite, mergedObj);
                            mergedObj.addRepresentedObj(getNewObj(allocSite));
                        }
                    });
                });
        return mergeMap;
    }

    @Override
    protected Obj doGetObj(New allocSite) {
        return Objects.requireNonNullElse(mergeMap.get(allocSite),
                getNewObj(allocSite));
    }
}
