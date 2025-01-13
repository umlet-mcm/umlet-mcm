package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfigurationUpdaterImpl implements ConfigurationUpdater {
    private final ModelUpdater modelUpdater;
    @Override
    public void updateConfiguration(Configuration configuration, Configuration configurationToUpdate) {
        if(configuration == null || configurationToUpdate == null) {
            return;
        }

        // Set the name of the configuration
        configurationToUpdate.setName(configuration.getName());

        // Set the models of the configuration
        for (val modelToUpdate : configurationToUpdate.getModels()){
            // Find the corresponding model entity
            val model = configuration.getModels().stream()
                    .filter(modelItem -> {
                        assert modelItem.getId() != null;
                        return modelItem.getId().equals(modelToUpdate.getId());
                    })
                    .findFirst()
                    .orElse(null);
            modelUpdater.updateModel(model, modelToUpdate);
        }
    }
}
