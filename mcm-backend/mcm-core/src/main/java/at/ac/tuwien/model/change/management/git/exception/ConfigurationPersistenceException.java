package at.ac.tuwien.model.change.management.git.exception;


public class ConfigurationPersistenceException extends RuntimeException {
    public ConfigurationPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationPersistenceException(String message) {
        super(message);
    }
}
