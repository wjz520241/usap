

package keeno.usap.analysis.pta.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.Analysis;
import keeno.usap.util.Timer;

/**
 * Records the elapsed time of pointer analysis.
 */
public class AnalysisTimer implements Plugin {

    private static final Logger logger = LogManager.getLogger(Analysis.class);

    private Timer ptaTimer;

    @Override
    public void onStart() {
        ptaTimer = new Timer("Pointer analysis");
        ptaTimer.start();
    }

    @Override
    public void onFinish() {
        ptaTimer.stop();
        logger.info(ptaTimer);
    }
}
