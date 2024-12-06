package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationDtoMapperTest extends MapperTest {

    private ConfigurationDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ConfigurationDtoMapper.class);
    }

    @Test
    void testToDto() {
        Configuration configuration = new Configuration();
        configuration.setName("Test Config");
        configuration.setVersion("1.0");
        configuration.setModels(Set.of(new Model()));

        ConfigurationDTO dto = mapper.toDto(configuration);

        assertNotNull(dto);
        assertEquals(configuration.getName(), dto.name());
        assertEquals(configuration.getVersion(), dto.version());
        assertNotNull(dto.models());
        assertFalse(dto.models().isEmpty());
    }

    @Test
    void testFromDto() {
        ModelDTO modelDTO = getModelDTO(Set.of(), "model-123");

        ConfigurationDTO dto = new ConfigurationDTO(
                "Test Config",
                "1.0",
                Set.of(modelDTO)
        );

        Configuration configuration = mapper.fromDto(dto);

        assertNotNull(configuration);
        assertEquals("Test Config", configuration.getName());
        assertEquals("1.0", configuration.getVersion());
        assertNotNull(configuration.getModels());
        assertFalse(configuration.getModels().isEmpty());
    }
}
