package at.ac.tuwien.model.change.management.core.exception;

public class ConfigurationVersionDoesNotExistException extends ConfigurationException {

    public ConfigurationVersionDoesNotExistException(String message) {
        super(message);
    }

    public ConfigurationVersionDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
