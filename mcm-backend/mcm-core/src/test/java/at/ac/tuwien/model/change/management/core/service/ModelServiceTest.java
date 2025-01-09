package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.git.repository.VersionControlRepository;
import at.ac.tuwien.model.change.management.testutil.MockConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ModelServiceTest {
    private ConfigurationRepository configurationRepository;
    private AutoCloseable annotations;

    @Mock
    private VersionControlRepository versionControlRepository;

    private ConfigurationService configurationService;
    private ModelService modelService;

    @Mock
    private GraphDBService graphDBService;

    @BeforeEach
    public void setup() {
        configurationRepository = new MockConfigurationRepository();
        annotations = MockitoAnnotations.openMocks(this);
        configurationService = new ConfigurationServiceImpl(configurationRepository, versionControlRepository, graphDBService);
        modelService = new ModelServiceImpl(configurationService);
    }

    @AfterEach
    public void teardown() throws Exception {
        annotations.close();
    }

    @Test
    public void testDeleteModelValidID() {
        var configuration = new Configuration();
        configuration.setName("test");
        var model = new Model();
        model.setId("id");
        configuration.getModels().add(model);
        when(versionControlRepository.getCurrentVersion(anyString())).thenAnswer(invocation ->
                findVersionByName(invocation.getArgument(0)));

        configurationService.createConfiguration(configuration);
        Assertions.assertDoesNotThrow(() -> modelService.deleteModel(model.getId()));

        var emptyConf = configurationService.getConfigurationByName(configuration.getName());
        Assertions.assertEquals(0, emptyConf.getModels().size());
    }

    @Test
    public void testDeleteModelInvalidID() {
        int confCount = configurationService.getAllConfigurations().size();
        Assertions.assertThrows(ModelNotFoundException.class, () -> modelService.deleteModel("invalid id"));
        Assertions.assertEquals(confCount, configurationService.getAllConfigurations().size());
    }

    private String findVersionByName(String name) {
        return configurationRepository.findConfigurationByName(name).orElseThrow().getVersion();
    }
}
