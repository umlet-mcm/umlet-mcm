package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.graphdb.entities.ConfigurationEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * The currently used implementation of the ConfigurationEntityMapper
 */
@Component
@AllArgsConstructor
public class ConfigurationEntityMapperImpl implements ConfigurationEntityMapper {
    private ModelEntityMapper modelEntityMapper;
    @Override
    public ConfigurationEntity toEntity(Configuration configuration) {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();

        // Set the name of the configuration
        configurationEntity.setName(configuration.getName());

        // Set the models of the configuration
        configurationEntity.setModels(configuration.getModels().stream().map(model -> modelEntityMapper.toEntity(model)).collect(Collectors.toSet()));

        return configurationEntity;
    }

    @Override
    public Configuration fromEntity(ConfigurationEntity configurationEntity) {
        Configuration configuration = new Configuration();

        // Set the name of the configuration
        configuration.setName(configurationEntity.getName());

        // Set the models of the configuration
        configuration.setModels(configurationEntity.getModels().stream().map(modelEntity -> modelEntityMapper.fromEntity(modelEntity)).collect(Collectors.toSet()));
        
        return configuration;
    }
}
