

package keeno.usap.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Reconfigurable;

import java.io.File;

/**
 * Utility class for configuring the log4j2 logger.
 */
public final class LoggerConfigs {

    private static final Logger logger = LogManager.getLogger(LoggerConfigs.class);

    private static final String LOG_FILE = "tai-e.log";

    /**
     * The name of the console appender added in log4j2.yml
     */
    private static final String CONSOLE_APPENDER_NAME = "STDOUT";

    /**
     * The name of the file appender which will be added to the root logger
     */
    private static final String FILE_APPENDER_NAME = "FILE";

    private LoggerConfigs() {
    }

    /**
     * Re-fetch the configuration from log4j2.yml and reconfigure the log4j2 logger.
     */
    public static void reconfigure() {
        // stop the last configuration
        Configuration config = ((LoggerContext) LogManager.getContext(false))
                .getConfiguration();
        config.stop();
        // re-fetch the configuration and reconfigure it
        if (config instanceof Reconfigurable reconfigurableConfig) {
            Configurator.reconfigure(reconfigurableConfig.reconfigure());
        }
    }

    /**
     * Set the log output file based on the given output dir.
     */
    public static void setOutput(File outputDir) {
        Configuration config = ((LoggerContext) LogManager.getContext(false))
                .getConfiguration();
        // new a file appender
        FileAppender fileAppender = FileAppender
                .newBuilder()
                .setName(FILE_APPENDER_NAME)
                .setLayout(config.getAppender(CONSOLE_APPENDER_NAME).getLayout())
                .withFileName(new File(outputDir, LOG_FILE).getAbsolutePath())
                .withAppend(false)
                .build();
        // add the appender to the configuration
        config.addAppender(fileAppender);
        // add the appender ref to the root logger
        LoggerConfig rootLogger = config.getRootLogger();
        rootLogger.addAppender(fileAppender, rootLogger.getLevel(), rootLogger.getFilter());
        fileAppender.start();
        logger.info("Writing log to {}", fileAppender.getFileName());
    }
}
