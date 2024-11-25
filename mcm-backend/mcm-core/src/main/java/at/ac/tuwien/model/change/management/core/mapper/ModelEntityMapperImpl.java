package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.graphdb.entities.ModelEntity;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * The currently used implementation of the ModelEntityMapper
 */
@Component
@AllArgsConstructor
public class ModelEntityMapperImpl implements ModelEntityMapper {
    private final static String ID = "Id";

    private NodeEntityMapper nodeMapper;
    @Override
    public ModelEntity toEntity(Model model) {
        if(model == null) {
            return null;
        }

        ModelEntity modelEntity = new ModelEntity();

        // Assign ID
        if(model.getId() != null) {
            modelEntity.setId(model.getId());
        }

        // Assign nodes
        modelEntity.setNodes(model.getNodes().stream().map(node -> nodeMapper.toEntity(node)).collect(Collectors.toSet()));

        return modelEntity;
    }

    @Override
    public Model fromEntity(ModelEntity modelEntity) {
        if(modelEntity == null) {
            return null;
        }

        Model model = new Model();

        // Assign ID
        if(modelEntity.getId() != null) {
            val mcmAttributes = new HashMap<String, Object>();
            mcmAttributes.put(ID, modelEntity.getId());
            model.setMcmAttributes(mcmAttributes);
        }

        // Assign nodes
        model.setNodes(modelEntity.getNodes().stream().map(nodeEntity -> nodeMapper.fromEntity(nodeEntity)).collect(Collectors.toSet()));

        return model;
    }
}
