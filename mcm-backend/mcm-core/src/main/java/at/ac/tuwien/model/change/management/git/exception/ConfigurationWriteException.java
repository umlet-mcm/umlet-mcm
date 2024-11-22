package at.ac.tuwien.model.change.management.git.exception;

public class ConfigurationWriteException extends ConfigurationPersistenceException {
    public ConfigurationWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationWriteException(String message) {
        super(message);
    }
}
