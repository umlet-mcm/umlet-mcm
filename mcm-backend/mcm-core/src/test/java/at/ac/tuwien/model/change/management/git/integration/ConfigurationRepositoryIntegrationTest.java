package at.ac.tuwien.model.change.management.git.integration;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.testutil.DomainModelGen;
import at.ac.tuwien.model.change.management.testutil.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.util.Objects;

@SpringBootTest
@ContextConfiguration(classes = GitTestConfig.class)
public class ConfigurationRepositoryIntegrationTest {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private GitProperties gitProperties;

    @BeforeEach
    public void setUp(@TempDir Path testDirectory) {
        // not the cleanest solution, but Spring implementations like DynamicPropertySource only work
        // with static methods, so @BeforeEach is not possible
        gitProperties.setRepositories(testDirectory);
    }

    @Test
    public void testCreate_shouldSucceed() {
        var configurationName = "test";
        Assertions.assertThatCode(() -> configurationRepository.createConfiguration(configurationName))
                .doesNotThrowAnyException();
    }

    @Test
    public void testCreate_createSameConfigurationTwice_shouldThrowRepositoryAlreadyExistsException() throws RepositoryAlreadyExistsException {
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
    public void testFindConfigurationByName_existingConfiguration_shouldReturnConfiguration()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        saveNewConfiguration(configuration);
        var readConfiguration = configurationRepository.findConfigurationByName(configurationName);
        Assertions.assertThat(readConfiguration).get()
                .extracting(Configuration::getVersion)
                .isNotNull();
        Assertions.assertThat(readConfiguration).get()
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testFindConfigurationByName_existingLargeConfiguration_shouldReturnConfiguration()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 5, 5, 4);
        saveNewConfiguration(configuration);
        var readConfiguration = configurationRepository.findConfigurationByName(configurationName);
        Assertions.assertThat(readConfiguration).get()
                .extracting(Configuration::getVersion)
                .isNotNull();
        Assertions.assertThat(readConfiguration).get()
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testFindAllConfigurations_noConfigurations_shouldReturnEmptyList() {
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).isEmpty();
    }

    @Test
    public void testFindAllConfigurations_oneConfiguration_shouldReturnListWithOneConfiguration()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        saveNewConfiguration(configuration);
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).hasSize(1)
                .first()
                .extracting(Configuration::getVersion)
                .isNotNull();
        Assertions.assertThat(configurations).first()
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testFindAllConfigurations_threeConfigurations_shouldReturnListWithThreeConfigurations()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationOne = DomainModelGen.generateRandomizedConfiguration("test1", 2, 2, 1);
        var configurationTwo = DomainModelGen.generateRandomizedConfiguration("test2", 3, 3, 2);
        var configurationThree = DomainModelGen.generateRandomizedConfiguration("test3", 4, 4, 1);
        saveNewConfiguration(configurationOne);
        saveNewConfiguration(configurationTwo);
        saveNewConfiguration(configurationThree);
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).hasSize(3)
                .extracting(Configuration::getVersion)
                .allMatch(Objects::nonNull);
        Assertions.assertThat(configurations)
                .usingRecursiveFieldByFieldElementComparator(TestUtils.recursiveConfigurationComparison())
                .containsExactlyInAnyOrder(configurationOne, configurationTwo, configurationThree);
    }

    @Test
    public void testSaveConfiguration_nonExistingConfiguration_shouldThrowRepositoryDoesNotExistException() {
        var configuration = new Configuration();
        configuration.setName("test");
        Assertions.assertThatCode(() -> configurationRepository.saveConfiguration(configuration))
                .isInstanceOf(RepositoryDoesNotExistException.class);
    }

    @Test
    public void testSaveConfiguration_existingConfiguration_shouldSucceed()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 2, 2, 1);
        configurationRepository.createConfiguration(configuration.getName());
        var savedConfiguration = configurationRepository.saveConfiguration(configuration);
        Assertions.assertThat(savedConfiguration)
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testSaveConfiguration_existingLargeConfiguration_shouldSucceed()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 2, 10, 5);
        configurationRepository.createConfiguration(configuration.getName());
        var savedConfiguration = configurationRepository.saveConfiguration(configuration);
        Assertions.assertThat(savedConfiguration)
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(configuration);
    }

    @Test
    public void testSaveConfiguration_updateExistingConfiguration_shouldSucceed()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 2, 2, 1);
        configurationRepository.createConfiguration(configuration.getName());
        configurationRepository.saveConfiguration(configuration);
        var updatedConfiguration = DomainModelGen.generateRandomizedConfiguration(configurationName, 3, 3, 2);
        var savedConfiguration = configurationRepository.saveConfiguration(updatedConfiguration);
        Assertions.assertThat(savedConfiguration)
                .usingRecursiveComparison(TestUtils.recursiveConfigurationComparison())
                .isEqualTo(updatedConfiguration);
    }


    @Test
    public void testDeleteConfiguration_existingConfiguration_shouldSucceed()
            throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 2, 2, 1);
        configurationRepository.createConfiguration(configuration.getName());
        configurationRepository.saveConfiguration(configuration);
        configurationRepository.deleteConfiguration(configurationName);
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).isEmpty();
    }

    @Test
    public void testDeleteConfiguration_nonExistingConfiguration_shouldSucceed() {
        var configurationName = "test";
        Assertions.assertThatCode(() -> configurationRepository.deleteConfiguration(configurationName))
                .doesNotThrowAnyException();
    }


    private void saveNewConfiguration(Configuration configuration) throws RepositoryAlreadyExistsException, RepositoryDoesNotExistException {
        configurationRepository.createConfiguration(configuration.getName());
        configurationRepository.saveConfiguration(configuration);
    }

}
