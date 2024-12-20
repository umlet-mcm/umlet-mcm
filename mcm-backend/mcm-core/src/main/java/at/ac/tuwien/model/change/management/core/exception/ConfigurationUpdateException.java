package at.ac.tuwien.model.change.management.core.exception;

public class ConfigurationUpdateException extends ConfigurationException {

    public ConfigurationUpdateException(String message) {
        super(message);
    }

    public ConfigurationUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
