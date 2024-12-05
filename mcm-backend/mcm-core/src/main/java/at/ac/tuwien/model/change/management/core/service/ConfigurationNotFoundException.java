package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;

public class ConfigurationNotFoundException extends ConfigurationException {
    public ConfigurationNotFoundException(String message) {
        super(message);
    }
}
