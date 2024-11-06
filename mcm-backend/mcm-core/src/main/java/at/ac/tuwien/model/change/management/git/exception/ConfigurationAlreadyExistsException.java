package at.ac.tuwien.model.change.management.git.exception;

public class ConfigurationAlreadyExistsException extends PersistenceException {
    public ConfigurationAlreadyExistsException(String message) {
        super(message);
    }
}
