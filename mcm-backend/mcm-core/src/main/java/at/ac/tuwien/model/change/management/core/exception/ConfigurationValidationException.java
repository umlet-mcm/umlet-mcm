package at.ac.tuwien.model.change.management.core.exception;

public class ConfigurationValidationException extends ConfigurationException {

    public ConfigurationValidationException(String message) {
        super(message);
    }

    public ConfigurationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
