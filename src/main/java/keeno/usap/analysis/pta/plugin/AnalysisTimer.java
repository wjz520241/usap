

package keeno.usap.analysis.pta.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.analysis.Analysis;
import keeno.usap.util.Timer;

/**
 * 记录指针分析的运行时间。
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
