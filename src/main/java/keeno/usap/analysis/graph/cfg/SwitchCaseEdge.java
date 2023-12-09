

package keeno.usap.analysis.graph.cfg;

class SwitchCaseEdge<N> extends CFGEdge<N> {

    private final int caseValue;

    SwitchCaseEdge(N source, N target, int caseValue) {
        super(Kind.SWITCH_CASE, source, target);
        this.caseValue = caseValue;
    }

    @Override
    public int getCaseValue() {
        return caseValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SwitchCaseEdge<?> that = (SwitchCaseEdge<?>) o;
        return caseValue == that.caseValue;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + caseValue;
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + " [" + caseValue + "]";
    }
}
