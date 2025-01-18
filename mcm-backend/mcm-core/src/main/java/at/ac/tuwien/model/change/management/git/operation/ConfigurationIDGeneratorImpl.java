package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.RepositoryWriteException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@GitComponent
public class ConfigurationIDGeneratorImpl implements ConfigurationIDGenerator {

    @Override
    public String setID(@NonNull BaseAttributes configurationElement, boolean allowOverwrite) {
        log.debug("Setting ID for model element {} with current ID '{}'",
                configurationElement.getTitle(), Objects.toString(configurationElement.getId(), "unassigned"));
        if (configurationElement.getId() == null || allowOverwrite) {
            var id = generateID();
            configurationElement.setId(id);
            log.debug("Generated new ID for model element: {}", configurationElement.getTitle());
            return configurationElement.getId();
        }
        throw new RepositoryWriteException("ID already set for model element, but override was not allowed: " + configurationElement.getTitle());
    }

    @Override
    public String generateID() {
        return UUID.randomUUID().toString();
    }
}
