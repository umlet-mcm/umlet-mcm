package at.ac.tuwien.model.change.management.git.exception;

public class ConfigurationDeleteException extends ConfigurationPersistenceException {
    public ConfigurationDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationDeleteException(String message) {
        super(message);
    }
}
