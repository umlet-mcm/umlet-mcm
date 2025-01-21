package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.mapper.uxf.ModelUxfMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.utils.PositionUtils;
import at.ac.tuwien.model.change.management.core.model.utils.RelationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
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

    @Override
    public List<Model> alignModels(List<Model> models) {
        ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);
        List<ModelUxf> modelUxfs = models.stream().map(modelUxfMapper::fromModel).toList();

        PositionUtils.alignModels(modelUxfs);

        return modelUxfs.stream().map(m->{
            Model model = modelUxfMapper.toModel(m);
            return RelationUtils.processRelations(model);
        }).toList();
    }
}
