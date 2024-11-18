package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationDtoMapperTest {

    private final ConfigurationDtoMapper mapper = Mappers.getMapper(ConfigurationDtoMapper.class);

    @Test
    void testToDto() {
        Model model = new Model();
        Configuration configuration = new Configuration();

        configuration.setName("name");
        configuration.setModels(Set.of(model));

        ConfigurationDTO configurationDto = mapper.toDto(configuration);

        assertNotNull(configurationDto);
        assertEquals(configuration.getName(), configurationDto.name());
        assertEquals(1, configurationDto.models().size());
    }

    @Test
    void testFromDto() {
        ModelDTO modelDto = new ModelDTO("1", Set.of());
        ConfigurationDTO configurationDto = new ConfigurationDTO("test", Set.of(modelDto));

        Configuration configuration = mapper.fromDto(configurationDto);

        assertNotNull(configuration);
        assertEquals(configurationDto.name(), configuration.getName());
        assertEquals(1, configuration.getModels().size());
    }
}
