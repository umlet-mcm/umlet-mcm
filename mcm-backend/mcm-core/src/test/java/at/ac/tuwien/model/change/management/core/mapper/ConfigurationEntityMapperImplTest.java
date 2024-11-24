package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.graphdb.entities.ConfigurationEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.ModelEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigurationEntityMapperImplTest {

    @Mock
    private ModelEntityMapper modelEntityMapper;

    @InjectMocks
    private ConfigurationEntityMapperImpl configurationEntityMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toEntity_withValidConfiguration_returnsCorrectConfigurationEntity() {
        Configuration configuration = new Configuration();
        configuration.setName("Config Name");
        Model model = new Model();
        configuration.setModels(Set.of(model));

        ModelEntity modelEntity = new ModelEntity();
        when(modelEntityMapper.toEntity(model)).thenReturn(modelEntity);

        ConfigurationEntity result = configurationEntityMapper.toEntity(configuration);

        assertEquals("Config Name", result.getName());
        assertEquals(Set.of(modelEntity), result.getModels());
    }

    @Test
    void toEntity_withNullConfiguration_returnsNull() {
        ConfigurationEntity result = configurationEntityMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void fromEntity_withValidConfigurationEntity_returnsCorrectConfiguration() {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();
        configurationEntity.setName("Config Name");
        ModelEntity modelEntity = new ModelEntity();
        configurationEntity.setModels(Set.of(modelEntity));

        Model model = new Model();
        when(modelEntityMapper.fromEntity(modelEntity)).thenReturn(model);

        Configuration result = configurationEntityMapper.fromEntity(configurationEntity);

        assertEquals("Config Name", result.getName());
        assertEquals(Set.of(model), result.getModels());
    }

    @Test
    void fromEntity_withNullConfigurationEntity_returnsNull() {
        Configuration result = configurationEntityMapper.fromEntity(null);

        assertNull(result);
    }

    @Test
    void toEntity_withEmptyModels_returnsConfigurationEntityWithEmptyModels() {
        Configuration configuration = new Configuration();
        configuration.setName("Config Name");
        configuration.setModels(Set.of());

        ConfigurationEntity result = configurationEntityMapper.toEntity(configuration);

        assertEquals("Config Name", result.getName());
        assertTrue(result.getModels().isEmpty());
    }

    @Test
    void fromEntity_withEmptyModels_returnsConfigurationWithEmptyModels() {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();
        configurationEntity.setName("Config Name");
        configurationEntity.setModels(Set.of());

        Configuration result = configurationEntityMapper.fromEntity(configurationEntity);

        assertEquals("Config Name", result.getName());
        assertTrue(result.getModels().isEmpty());
    }
}