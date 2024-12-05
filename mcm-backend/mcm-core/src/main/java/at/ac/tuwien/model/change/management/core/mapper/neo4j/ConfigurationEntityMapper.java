package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.graphdb.entities.ConfigurationEntity;

/**
 * Mapper for converting between Configuration and ConfigurationEntity
 */
public interface ConfigurationEntityMapper {
    /**
     * Converts a Configuration to a ConfigurationEntity
     * @param configuration the Configuration to convert
     * @return the converted ConfigurationEntity
     */
    ConfigurationEntity toEntity(Configuration configuration);

    /**
     * Converts a ConfigurationEntity to a Configuration
     * @param configurationEntity the ConfigurationEntity to convert
     * @return the converted Configuration
     */
    Configuration fromEntity(ConfigurationEntity configurationEntity);
}
