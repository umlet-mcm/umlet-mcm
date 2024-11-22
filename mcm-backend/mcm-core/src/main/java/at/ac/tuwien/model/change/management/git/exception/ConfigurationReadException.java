package at.ac.tuwien.model.change.management.git.exception;

public class ConfigurationReadException extends ConfigurationPersistenceException {
    public ConfigurationReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationReadException(String message) {
        super(message);
    }
}
