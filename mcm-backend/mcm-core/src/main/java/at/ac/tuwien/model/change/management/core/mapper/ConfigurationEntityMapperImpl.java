package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.graphdb.entities.ConfigurationEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * The currently used implementation of the ConfigurationEntityMapper
 */
@Component
public class ConfigurationEntityMapperImpl implements ConfigurationEntityMapper {
    private ModelEntityMapper modelEntityMapper;
    @Override
    public ConfigurationEntity toEntity(Configuration configuration) {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();
        configurationEntity.setName(configuration.getName());
        configurationEntity.setModels(configuration.getModels().stream().map(model -> modelEntityMapper.toEntity(model)).collect(Collectors.toSet()));
        return configurationEntity;
    }

    @Override
    public Configuration fromEntity(ConfigurationEntity configurationEntity) {
        Configuration configuration = new Configuration();
        configuration.setName(configurationEntity.getName());
        configuration.setModels(configurationEntity.getModels().stream().map(modelEntity -> modelEntityMapper.fromEntity(modelEntity)).collect(Collectors.toSet()));
        return configuration;
    }
}
