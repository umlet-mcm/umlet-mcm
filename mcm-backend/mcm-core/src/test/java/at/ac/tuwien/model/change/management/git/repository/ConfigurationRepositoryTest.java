package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.operation.ConfigurationIOManager;
import at.ac.tuwien.model.change.management.git.operation.RepositoryManager;
import at.ac.tuwien.model.change.management.git.util.VersionControlUtils;
import at.ac.tuwien.model.change.management.git.operation.MockConfigurationIOManager;
import at.ac.tuwien.model.change.management.git.operation.MockRepositoryManager;
import at.ac.tuwien.model.change.management.testutil.DomainModelGen;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class ConfigurationRepositoryTest {

    @TempDir
    private Path testDirectory;

    private RepositoryManager repositoryManager;
    private ConfigurationIOManager configurationIOManager;
    private ConfigurationRepository configurationRepository;

    @BeforeEach
    public void setUp() {
        repositoryManager = new MockRepositoryManager(testDirectory);
        configurationIOManager = new MockConfigurationIOManager();
        configurationRepository = new ConfigurationRepositoryImpl(configurationIOManager, repositoryManager);
    }

    @Test
    public void testCreate_shouldSucceed() {
        var configurationName = "test";
        Assertions.assertThatCode(() -> configurationRepository.createConfiguration(configurationName))
                .doesNotThrowAnyException();
    }

    @Test
    public void testCreate_createSameConfigurationTwice_shouldThrowRepositoryAlreadyExistsException()
            throws RepositoryAlreadyExistsException {
        var configurationName = "test";
        configurationRepository.createConfiguration(configurationName);
        Assertions.assertThatCode(() -> configurationRepository.createConfiguration(configurationName))
                .isInstanceOf(RepositoryAlreadyExistsException.class);
    }

    @Test
    public void testFindConfigurationByName_nonExistingConfiguration_shouldReturnEmptyOptional() {
        var configurationName = "test";
        var configuration = configurationRepository.findConfigurationByName(configurationName);
        Assertions.assertThat(configuration).isEmpty();
    }

    @Test
    public void testFindConfigurationByName_existingConfiguration_shouldReturnConfiguration() throws GitAPIException {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        saveNewConfiguration(configuration);
        var readConfiguration = configurationRepository.findConfigurationByName(configurationName);
        Assertions.assertThat(readConfiguration)
                .get()
                .usingRecursiveComparison()
                .ignoringFields("version")
                .isEqualTo(configuration);
    }

    @Test
    public void testFindAllConfigurations_noConfigurations_shouldReturnEmptyList() {
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).isEmpty();
    }

    @Test
    public void testFindAllConfigurations_oneConfiguration_shouldReturnListWithOneConfiguration() throws GitAPIException {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        saveNewConfiguration(configuration);
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).hasSize(1)
                .first()
                .usingRecursiveComparison()
                .ignoringFields("version")
                .isEqualTo(configuration);
    }

    @Test
    public void testFindAllConfigurations_threeConfigurations_shouldReturnListWithThreeConfigurations() throws GitAPIException {
        var configurationOne = DomainModelGen.generateRandomizedConfiguration("test1", 1, 1, 1);
        var configurationTwo = DomainModelGen.generateRandomizedConfiguration("test2", 2, 2, 2);
        var configurationThree = DomainModelGen.generateRandomizedConfiguration("test3", 3, 3, 3);
        saveNewConfiguration(configurationOne);
        saveNewConfiguration(configurationTwo);
        saveNewConfiguration(configurationThree);
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations)
                .hasSize(3)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("version")
                .containsExactlyInAnyOrder(configurationOne, configurationTwo, configurationThree);
    }


    @Test
    public void testSaveConfiguration_existingConfiguration_shouldUpdateConfiguration() throws GitAPIException, RepositoryDoesNotExistException {
        var configuration = DomainModelGen.generateRandomizedConfiguration("test", 1, 1, 1);
        saveNewConfiguration(configuration);
        var updatedConfiguration = DomainModelGen.generateRandomizedConfiguration("test", 2, 2, 2);
        configurationRepository.saveConfiguration(updatedConfiguration);
        var readConfiguration = configurationRepository.findConfigurationByName(updatedConfiguration.getName());
        Assertions.assertThat(readConfiguration)
                .get()
                .usingRecursiveComparison()
                .ignoringFields("version")
                .isEqualTo(updatedConfiguration);
    }

    @Test
    public void testSaveConfiguration_nonExistingConfiguration_shouldThrowRepositoryDoesNotExistException() {
        var configuration = DomainModelGen.generateRandomizedConfiguration("test", 1, 1, 1);
        Assertions.assertThatCode(() -> configurationRepository.saveConfiguration(configuration))
                .isInstanceOf(RepositoryDoesNotExistException.class);
    }

    @Test
    public void testDeleteConfiguration_existingConfiguration_shouldDeleteConfiguration() throws GitAPIException {
        var configuration = DomainModelGen.generateRandomizedConfiguration("test", 1, 1, 1);
        saveNewConfiguration(configuration);
        configurationRepository.deleteConfiguration(configuration.getName());
        var readConfiguration = configurationRepository.findConfigurationByName(configuration.getName());
        Assertions.assertThat(readConfiguration).isEmpty();
    }

    @Test
    public void testDeleteConfiguration_nonExistingConfiguration_shouldNotThrowException() {
        var configurationName = "test";
        Assertions.assertThatCode(() -> configurationRepository.deleteConfiguration(configurationName))
                .doesNotThrowAnyException();
    }

    private void saveNewConfiguration(Configuration configuration) throws GitAPIException {
        try (var repository = repositoryManager.accessRepository(configuration.getName())) {
            VersionControlUtils.initRepository(repository);
            configurationIOManager.writeConfigurationToRepository(repository, configuration);
        }
    }
}
