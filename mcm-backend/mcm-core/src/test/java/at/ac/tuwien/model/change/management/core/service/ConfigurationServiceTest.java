package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationAlreadyExistsException;
import at.ac.tuwien.model.change.management.core.exception.ConfigurationDoesNotExistException;
import at.ac.tuwien.model.change.management.core.exception.ConfigurationUpdateException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.git.repository.VersionControlRepository;
import at.ac.tuwien.model.change.management.git.repository.MockConfigurationRepository;
import at.ac.tuwien.model.change.management.testutil.DomainModelGen;
import at.ac.tuwien.model.change.management.testutil.TestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ConfigurationServiceTest {

    private ConfigurationRepository configurationRepository;
    private AutoCloseable annotations;

    @Mock
    private VersionControlRepository versionControlRepository;

    private ConfigurationService configurationService;

    @BeforeEach
    public void setup() {
        configurationRepository = new MockConfigurationRepository();
        annotations = MockitoAnnotations.openMocks(this);
        configurationService = new ConfigurationServiceImpl(configurationRepository, versionControlRepository);
    }

    @AfterEach
    public void teardown() throws Exception {
        annotations.close();
    }

    @Test
    public void testCreateConfiguration_nonExistingConfiguration_shouldSucceed() {
        var configuration = new Configuration();
        configuration.setName("test");
        var result = configurationService.createConfiguration(configuration);
        Assertions.assertThat(result)
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testCreateConfiguration_existingConfiguration_shouldThrowConfigurationAlreadyExistsException() {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        configurationService.createConfiguration(configuration);

        var configurationWithExistingName = new Configuration();
        configurationWithExistingName.setName(configurationName);
        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configurationWithExistingName))
                .isInstanceOf(ConfigurationAlreadyExistsException.class);
    }

    @Test
    public void testUpdateConfiguration_existingConfiguration_shouldSucceed() throws RepositoryDoesNotExistException {
        when(versionControlRepository.getCurrentVersion(anyString())).thenAnswer(invocation ->
                findVersionByName(invocation.getArgument(0)));

        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 2, 2, 1);
        var originalConfiguration = configurationService.createConfiguration(configuration);

        var updatedConfiguration = DomainModelGen.generateRandomizedConfiguration(configurationName, 3, 3, 2);
        updatedConfiguration.setVersion(originalConfiguration.getVersion());

        var result = configurationService.updateConfiguration(updatedConfiguration);
        Assertions.assertThat(result)
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(updatedConfiguration);
    }

    @Test
    public void testUpdateConfiguration_nonExistingConfiguration_shouldThrowConfigurationDoesNotExistException() throws RepositoryDoesNotExistException {
        when(versionControlRepository.getCurrentVersion(anyString())).thenThrow(RepositoryDoesNotExistException.class);
        var configuration = new Configuration();
        configuration.setVersion(RandomStringUtils.randomAlphanumeric(40).toLowerCase());
        configuration.setName("test");
        Assertions.assertThatThrownBy(() -> configurationService.updateConfiguration(configuration))
                .isInstanceOf(ConfigurationDoesNotExistException.class);
    }

    @Test
    public void testUpdateConfiguration_existingConfigurationWithDifferentVersion_shouldThrowConfigurationUpdateException() {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 2, 2, 1);
        configurationService.createConfiguration(configuration);
        var updatedConfiguration = DomainModelGen.generateRandomizedConfiguration(configurationName, 3, 3, 2);
        updatedConfiguration.setVersion(RandomStringUtils.randomAlphanumeric(40).toLowerCase());

        Assertions.assertThatThrownBy(() -> configurationService.updateConfiguration(updatedConfiguration))
                .isInstanceOf(ConfigurationUpdateException.class);
    }

    @Test
    public void testDeleteConfiguration_existingConfiguration_shouldSucceed() {
        var configuration = new Configuration();
        configuration.setName("test");
        configurationService.createConfiguration(configuration);
        configurationService.deleteConfiguration(configuration.getName());
        Assertions.assertThat(configurationRepository.findConfigurationByName(configuration.getName())).isEmpty();
    }

    @Test
    public void testDeleteConfiguration_nonExistingConfiguration_shouldSucceed() {
        Assertions.assertThatCode(() -> configurationService.deleteConfiguration("test"))
                .doesNotThrowAnyException();
    }

    @Test
    public void testGetConfigurationByName_existingConfiguration_shouldSucceed() {
        var configuration = new Configuration();
        configuration.setName("test");
        configurationService.createConfiguration(configuration);
        var result = configurationService.getConfigurationByName(configuration.getName());
        Assertions.assertThat(result)
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testGetConfigurationByName_existingLargeConfiguration_shouldSucceed() {
        var configuration = DomainModelGen.generateRandomizedConfiguration("test", 5, 100, 20);
        configurationService.createConfiguration(configuration);
        var result = configurationService.getConfigurationByName(configuration.getName());
        Assertions.assertThat(result)
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testGetConfigurationByName_nonExistingConfiguration_shouldThrowConfigurationNotFoundException() {
        Assertions.assertThatThrownBy(() -> configurationService.getConfigurationByName("test"))
                .isInstanceOf(ConfigurationNotFoundException.class);
    }

    @Test
    public void testGetAllConfigurations_noConfigurations_shouldReturnEmptyList() {
        Assertions.assertThat(configurationService.getAllConfigurations()).isEmpty();
    }

    @Test
    public void testGetAllConfigurations_oneConfiguration_shouldReturnListWithOneConfiguration() {
        var configuration = new Configuration();
        configuration.setName("test");
        configurationService.createConfiguration(configuration);
        Assertions.assertThat(configurationService.getAllConfigurations()).hasSize(1)
                .usingRecursiveFieldByFieldElementComparator(TestUtils.recursiveConfigurationComparison())
                .containsExactly(configuration);
    }

    @Test
    public void testGetAllConfigurations_threeConfigurations_shouldReturnListWithThreeConfigurations() {
        var configurationOne = DomainModelGen.generateRandomizedConfiguration("test1", 2, 2, 1);
        var configurationTwo = DomainModelGen.generateRandomizedConfiguration("test2", 2, 2, 1);
        var configurationThree = DomainModelGen.generateRandomizedConfiguration("test3", 2, 2, 1);
        configurationService.createConfiguration(configurationOne);
        configurationService.createConfiguration(configurationTwo);
        configurationService.createConfiguration(configurationThree);
        Assertions.assertThat(configurationService.getAllConfigurations()).hasSize(3)
                .usingRecursiveFieldByFieldElementComparator(TestUtils.recursiveConfigurationComparison())
                .containsExactlyInAnyOrder(configurationOne, configurationTwo, configurationThree);
    }

    private String findVersionByName(String name) {
        return configurationRepository.findConfigurationByName(name).orElseThrow().getVersion();
    }
}
