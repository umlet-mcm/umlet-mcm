package at.ac.tuwien.model.change.management.core.exception;

public class ConfigurationResetException extends ConfigurationException {

    public ConfigurationResetException(String message) {
        super(message);
    }

    public ConfigurationResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
