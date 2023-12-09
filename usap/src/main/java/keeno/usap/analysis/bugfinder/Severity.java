

package keeno.usap.analysis.bugfinder;

public enum Severity {

    BLOCKER("High", "High"),
    CRITICAL("High", "Low"),
    MAJOR("Low", "High"),
    MINOR("Low", "Low");

    private final String impact;

    private final String likelihood;

    Severity(String impact, String likelihood) {
        this.impact = impact;
        this.likelihood = likelihood;
    }

    public String getImpact() {
        return impact;
    }

    public String getLikelihood() {
        return likelihood;
    }
}
