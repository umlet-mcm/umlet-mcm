package at.ac.tuwien.model.change.management.git.exception;

public class ConfigurationUpdateException extends ConfigurationPersistenceException {
    public ConfigurationUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
