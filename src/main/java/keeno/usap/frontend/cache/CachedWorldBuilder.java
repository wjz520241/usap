

package keeno.usap.frontend.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import keeno.usap.World;
import keeno.usap.WorldBuilder;
import keeno.usap.config.AnalysisConfig;
import keeno.usap.config.Options;
import keeno.usap.util.Timer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link WorldBuilder} that loads the cached world if it exists, or delegates to the
 * underlying {@link WorldBuilder} otherwise.
 */
public class CachedWorldBuilder implements WorldBuilder {

    private static final Logger logger = LogManager.getLogger(CachedWorldBuilder.class);

    private static final String CACHE_DIR = "cache";

    private final WorldBuilder delegate;

    public CachedWorldBuilder(WorldBuilder delegate) {
        this.delegate = delegate;
        logger.info("The world cache mode is enabled.");
    }

    @Override
    public void build(Options options, List<AnalysisConfig> analyses) {
        if (!options.isWorldCacheMode()) {
            logger.error("Using CachedWorldBuilder,"
                    + " but world cache mode option is not enabled");
            System.exit(-1);
        }
        File worldCacheFile = getWorldCacheFile(options);
        if (loadCache(options, worldCacheFile)) {
            return;
        }
        runWorldBuilder(options, analyses);
        saveCache(worldCacheFile);
    }

    private boolean loadCache(Options options, File worldCacheFile) {
        if (!worldCacheFile.exists()) {
            logger.info("World cache not found in {}", worldCacheFile);
            return false;
        }
        logger.info("Loading the world cache from {}", worldCacheFile);
        Timer timer = new Timer("Load the world cache");
        timer.start();
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(worldCacheFile)));
            World world = (World) ois.readObject();
            World.set(world);
            world.setOptions(options);
            return true;
        } catch (Exception e) {
            logger.error("Failed to load world cache from {} due to {}",
                    worldCacheFile, e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception e) {
                    logger.error("Failed to close input stream", e);
                }
            }
            timer.stop();
            logger.info(timer);
        }
        return false;
    }

    private void runWorldBuilder(Options options, List<AnalysisConfig> analyses) {
        logger.info("Running the WorldBuilder ...");
        Timer timer = new Timer("Run the WorldBuilder");
        timer.start();
        delegate.build(options, analyses);
        timer.stop();
        logger.info(timer);
    }

    private void saveCache(File worldCacheFile) {
        logger.info("Saving the world cache to {}", worldCacheFile);
        Timer timer = new Timer("Save the world cache");
        timer.start();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(worldCacheFile)));
            oos.writeObject(World.get());
            oos.close();
        } catch (Exception e) {
            logger.error("Failed to save world cache from {} due to {}",
                    worldCacheFile, e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception e) {
                    logger.error("Failed to close output stream", e);
                }
            }
            timer.stop();
            logger.info(timer);
        }
    }

    public static File getWorldCacheFile(Options options) {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return new File(cacheDir,
                "world-cache-" + getWorldCacheHash(options) + ".bin").getAbsoluteFile();
    }

    private static int getWorldCacheHash(Options options) {
        int result = options.getMainClass() != null
                ? options.getMainClass().hashCode() : 0;
        result = 31 * result + (options.getInputClasses() != null
                ? options.getInputClasses().hashCode() : 0);
        result = 31 * result + options.getJavaVersion();
        result = 31 * result + (options.isPrependJVM() ? 1 : 0);
        result = 31 * result + (options.isAllowPhantom() ? 1 : 0);
        result = 31 * result + (options.getWorldBuilderClass() != null
                ? options.getWorldBuilderClass().getName().hashCode() : 0);
        // add the timestamp to the cache key calculation
        List<String> paths = new ArrayList<>();
        paths.addAll(options.getClassPath());
        paths.addAll(options.getAppClassPath());
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                result = 31 * result + (int) file.lastModified();
            }
        }
        result = Math.abs(result);
        return result;
    }
}
