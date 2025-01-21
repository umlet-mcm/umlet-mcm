package at.ac.tuwien.model.change.management.testutil;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationValidationException;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.git.operation.IdGenerator;
import lombok.NonNull;

import java.util.UUID;

public class MockIdGenerator implements IdGenerator {

    @Override
    public String setID(@NonNull BaseAttributes configurationElement, boolean allowOverwrite) {
        if (configurationElement.getId() == null || allowOverwrite) {
            configurationElement.setId(generateID());
            return configurationElement.getId();
        }
        throw new ConfigurationValidationException("");
    }

    @Override
    public String generateID() {
        return UUID.randomUUID().toString();
    }
}
