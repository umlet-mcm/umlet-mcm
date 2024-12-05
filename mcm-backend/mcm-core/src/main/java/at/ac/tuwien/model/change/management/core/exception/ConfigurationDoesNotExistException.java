package at.ac.tuwien.model.change.management.core.exception;

public class ConfigurationDoesNotExistException extends ConfigurationException {

    public ConfigurationDoesNotExistException(String message) {
        super(message);
    }

    public ConfigurationDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
