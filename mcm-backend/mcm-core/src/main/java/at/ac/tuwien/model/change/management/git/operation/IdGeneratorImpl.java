package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationValidationException;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdGeneratorImpl implements IdGenerator {

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
        throw new ConfigurationValidationException("ID already set for model element '" + configurationElement.getTitle() +
                "' but should not have been");
    }

    @Override
    public String generateID() {
        return UUID.randomUUID().toString();
    }
}
