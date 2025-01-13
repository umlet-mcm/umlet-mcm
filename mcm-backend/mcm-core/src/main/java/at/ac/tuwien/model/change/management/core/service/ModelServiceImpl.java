package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelServiceImpl implements ModelService {
    private final ConfigurationService configurationService;

    @Override
    public void deleteModel(String modelId) {
        List<Configuration> configurations = configurationService.getAllConfigurations();
        for (Configuration configuration : configurations) {
            Iterator<Model> iterator = configuration.getModels().iterator();
            while (iterator.hasNext()) {
                Model model = iterator.next();
                if (model.getId().equals(modelId)) {
                    iterator.remove();
                    configurationService.updateConfiguration(configuration);
                    log.info("Model with id {} deleted from configuration {}", modelId, configuration.getName());
                    return;
                }
            }
        }

        throw new ModelNotFoundException("Model with id '" + modelId + "' not found");
    }
}
