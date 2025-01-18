package at.ac.tuwien.model.change.management.core.exception;

public class ConfigurationCheckoutException extends ConfigurationException {

    public ConfigurationCheckoutException(String message) {
        super(message);
    }

    public ConfigurationCheckoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
