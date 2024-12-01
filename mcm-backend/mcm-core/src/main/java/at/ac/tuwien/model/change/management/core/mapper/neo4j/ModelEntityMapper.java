package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.graphdb.entities.ModelEntity;

/**
 * Mapper for converting between Model and ModelEntity
 */
public interface ModelEntityMapper {
    /**
     * Converts a Model to a ModelEntity
     * @param model the Model to convert
     * @return the converted ModelEntity
     */
    ModelEntity toEntity(Model model);

    /**
     * Converts a ModelEntity to a Model
     * @param modelEntity the ModelEntity to convert
     * @return the converted Model
     */
    Model fromEntity(ModelEntity modelEntity);
}
