package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.graphdb.entities.ModelEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * The currently used implementation of the ModelEntityMapper
 */
@Component
public class ModelEntityMapperImpl implements ModelEntityMapper {
    private NodeEntityMapper nodeMapper;
    @Override
    public ModelEntity toEntity(Model model) {
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setId(model.getId());
        modelEntity.setNodes(model.getNodes().stream().map(node -> nodeMapper.toEntity(node)).collect(Collectors.toSet()));
        return modelEntity;
    }

    @Override
    public Model fromEntity(ModelEntity modelEntity) {
        Model model = new Model();
        model.setId(modelEntity.getId());
        model.setNodes(modelEntity.getNodes().stream().map(nodeEntity -> nodeMapper.fromEntity(nodeEntity)).collect(Collectors.toSet()));
        return model;
    }
}
