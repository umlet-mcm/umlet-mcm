package at.ac.tuwien.model.change.management.git.exception;

public class ConfigurationCreateException extends ConfigurationPersistenceException {
    public ConfigurationCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationCreateException(String message) {
        super(message);
    }
}
