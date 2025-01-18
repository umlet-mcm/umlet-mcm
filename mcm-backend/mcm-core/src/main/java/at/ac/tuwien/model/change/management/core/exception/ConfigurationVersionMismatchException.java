package at.ac.tuwien.model.change.management.core.exception;

public class ConfigurationVersionMismatchException extends ConfigurationException{

    public ConfigurationVersionMismatchException(String message) {
        super(message);
    }

    public ConfigurationVersionMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
