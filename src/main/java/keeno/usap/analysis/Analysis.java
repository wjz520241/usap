

package keeno.usap.analysis;

import keeno.usap.config.AnalysisConfig;
import keeno.usap.config.AnalysisOptions;
import keeno.usap.config.ConfigException;
import keeno.usap.util.AnalysisException;

import java.lang.reflect.Field;

/**
 * Abstract base class for all analyses.
 */
public abstract class Analysis {

    /**
     * Configuration of this analysis.
     */
    private final AnalysisConfig config;

    // private boolean isStoreResult;

    protected Analysis(AnalysisConfig config) {
        this.config = config;
        validateId();
    }

    public String getId() {
        return config.getId();
    }

    public AnalysisOptions getOptions() {
        return config.getOptions();
    }

    /**
     * Checks if this analysis class declares a public static field "ID"
     * and its value is identical to the analysis id in the configuration.
     */
    private void validateId() {
        Class<?> analysisClass = getClass();
        try {
            Field idField = analysisClass.getField("ID");
            String id = (String) idField.get(null);
            if (!id.equals(getId())) {
                throw new ConfigException(String.format(
                        "Config ID (%s) and analysis ID (%s) of %s are not matched",
                        getId(), id, analysisClass));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AnalysisException(String.format("Failed to get analysis ID of %s," +
                    " please add public static field 'ID' in %s" +
                    analysisClass, analysisClass));
        }
    }
}
