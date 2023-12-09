

package keeno.usap.config;

/**
 * This class represents the exceptions in configuration.
 */
public class ConfigException extends RuntimeException {

    public ConfigException(String msg) {
        super(msg);
    }

    public ConfigException(String msg, Throwable t) {
        super(msg, t);
    }
}
