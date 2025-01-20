package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.ConfigurationVersion;
import at.ac.tuwien.model.change.management.core.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ConfigurationUpdaterImplTest {

    @Mock
    private ModelUpdater modelUpdater;

    @InjectMocks
    private ConfigurationUpdaterImpl configurationUpdater;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateConfiguration_updatesNameAndModels() {
        Configuration configuration = new Configuration();
        configuration.setName("NewConfig");
        Set<Model> models = new HashSet<>();
        Model model1 = new Model();
        model1.setId("1");
        models.add(model1);
        configuration.setModels(models);
        configuration.setVersion(new ConfigurationVersion("123", null, null));

        Configuration configurationToUpdate = new Configuration();
        configurationToUpdate.setName("OldConfig");
        Set<Model> modelsToUpdate = new HashSet<>();
        Model modelToUpdate1 = new Model();
        modelToUpdate1.setId("1");
        modelsToUpdate.add(modelToUpdate1);
        configurationToUpdate.setModels(modelsToUpdate);
        configurationToUpdate.setVersion(new ConfigurationVersion("456", null, null));

        configurationUpdater.updateConfiguration(configuration, configurationToUpdate);

        assertEquals("NewConfig", configurationToUpdate.getName());
        assertEquals("456", configurationToUpdate.getVersionHash());
        verify(modelUpdater, times(1)).updateModel(model1, modelToUpdate1);
        assertEquals(1, configurationToUpdate.getModels().size());
    }

    @Test
    void updateConfiguration_nullConfiguration_doesNothing() {
        Configuration configurationToUpdate = new Configuration();
        configurationToUpdate.setName("OldConfig");

        configurationUpdater.updateConfiguration(null, configurationToUpdate);

        assertEquals("OldConfig", configurationToUpdate.getName());
        verifyNoInteractions(modelUpdater);
    }

    @Test
    void updateConfiguration_nullConfigurationToUpdate_doesNothing() {
        Configuration configuration = new Configuration();
        configuration.setName("NewConfig");

        configurationUpdater.updateConfiguration(configuration, null);

        verifyNoInteractions(modelUpdater);
    }

    @Test
    void updateConfiguration_noMatchingModels_doesNotUpdateModels() {
        Configuration configuration = new Configuration();
        configuration.setName("NewConfig");
        Set<Model> models = new HashSet<>();
        Model model1 = new Model();
        model1.setId("1");
        models.add(model1);
        configuration.setModels(models);

        Configuration configurationToUpdate = new Configuration();
        configurationToUpdate.setName("OldConfig");
        Set<Model> modelsToUpdate = new HashSet<>();
        Model modelToUpdate1 = new Model();
        modelToUpdate1.setId("2");
        modelsToUpdate.add(modelToUpdate1);
        configurationToUpdate.setModels(modelsToUpdate);

        configurationUpdater.updateConfiguration(configuration, configurationToUpdate);

        assertEquals("NewConfig", configurationToUpdate.getName());
        verify(modelUpdater, only()).updateModel(eq(null), any());
        assertEquals(1, configurationToUpdate.getModels().size());
        assertEquals("2", configurationToUpdate.getModels().iterator().next().getId());
    }
}