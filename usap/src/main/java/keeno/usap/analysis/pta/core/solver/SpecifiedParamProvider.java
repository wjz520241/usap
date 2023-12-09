

package keeno.usap.analysis.pta.core.solver;

import keeno.usap.analysis.pta.core.heap.Obj;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;
import keeno.usap.util.collection.Sets;
import keeno.usap.util.collection.TwoKeyMultiMap;

import java.util.Collections;
import java.util.Set;

/**
 * This {@link ParamProvider} returns this/parameter objects specified via its builder.
 * For non-specified this variable or parameters,
 * the {@link Builder#delegate}'s result will be returned.
 */
public class SpecifiedParamProvider implements ParamProvider {

    private final Set<Obj> thisObjs;

    private final Set<Obj>[] paramObjs;

    private final TwoKeyMultiMap<Obj, JField, Obj> fieldContents;

    private final MultiMap<Obj, Obj> arrayContents;

    private SpecifiedParamProvider(Set<Obj> thisObjs, Set<Obj>[] paramObjs,
                                   TwoKeyMultiMap<Obj, JField, Obj> fieldContents,
                                   MultiMap<Obj, Obj> arrayContents) {
        this.thisObjs = thisObjs;
        this.paramObjs = paramObjs;
        this.fieldContents = fieldContents;
        this.arrayContents = arrayContents;
    }

    @Override
    public Set<Obj> getThisObjs() {
        return thisObjs;
    }

    @Override
    public Set<Obj> getParamObjs(int i) {
        return paramObjs[i];
    }

    @Override
    public TwoKeyMultiMap<Obj, JField, Obj> getFieldObjs() {
        return fieldContents;
    }

    @Override
    public MultiMap<Obj, Obj> getArrayObjs() {
        return arrayContents;
    }

    // TODO: validate types of input this/param objects?
    public static class Builder {

        private ParamProvider delegate;

        private final JMethod method;

        private Set<Obj> thisObjs;

        private final Set<Obj>[] paramObjs;

        private final TwoKeyMultiMap<Obj, JField, Obj> fieldObjs;

        private final MultiMap<Obj, Obj> arrayObjs;

        @SuppressWarnings("unchecked")
        public Builder(JMethod method) {
            this.delegate = EmptyParamProvider.get();
            this.method = method;
            this.paramObjs = (Set<Obj>[]) new Set[method.getParamCount()];
            this.fieldObjs = Maps.newTwoKeyMultiMap();
            this.arrayObjs = Maps.newMultiMap();
        }

        public Builder setDelegate(ParamProvider delegate) {
            this.delegate = delegate;
            return this;
        }

        public Builder addThisObj(Obj thisObj) {
            if (thisObjs == null) {
                thisObjs = Sets.newHybridSet();
            }
            thisObjs.add(thisObj);
            return this;
        }

        public Builder addParamObj(int i, Obj paramObj) {
            if (paramObjs[i] == null) {
                paramObjs[i] = Sets.newHybridSet();
            }
            paramObjs[i].add(paramObj);
            return this;
        }

        public Builder addFieldObj(Obj base, JField field, Obj obj) {
            fieldObjs.put(base, field, obj);
            return this;
        }

        public Builder addArrayObj(Obj array, Obj elem) {
            arrayObjs.put(array, elem);
            return this;
        }

        public SpecifiedParamProvider build() {
            if (thisObjs == null) {
                thisObjs = delegate.getThisObjs();
            }
            thisObjs = Collections.unmodifiableSet(thisObjs);
            for (int i = 0; i < method.getParamCount(); ++i) {
                if (paramObjs[i] == null) {
                    paramObjs[i] = delegate.getParamObjs(i);
                }
                paramObjs[i] = Collections.unmodifiableSet(paramObjs[i]);
            }
            delegate.getFieldObjs().forEach((base, field, obj) -> {
                if (!fieldObjs.containsKey(base, field)) {
                    fieldObjs.put(base, field, obj);
                }
            });
            delegate.getArrayObjs().forEach((array, elem) -> {
                if (!arrayObjs.containsKey(array)) {
                    arrayObjs.put(array, elem);
                }
            });
            return new SpecifiedParamProvider(thisObjs, paramObjs,
                    Maps.unmodifiableTwoKeyMultiMap(fieldObjs),
                    Maps.unmodifiableMultiMap(arrayObjs));
        }
    }
}
