package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.versioning.ModelDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.NodeDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.RelationDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationProcessor;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.integration.GitTestConfig;
import at.ac.tuwien.model.change.management.git.util.PathUtils;
import at.ac.tuwien.model.change.management.testutil.DomainModelGen;
import at.ac.tuwien.model.change.management.testutil.assertion.ConfigurationAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@SpringBootTest
@ContextConfiguration(classes = GitTestConfig.class)
public class ConfigurationRepositoryIntegrationTest {

    @Autowired
    private ConfigurationRepository configurationRepository;

    private final static String TEST_CONFIGURATION_NAME = "test-configuration";
    private final static String TEST_CONFIGURATION_VERSION = "v1.0.0";

    @TempDir
    private static Path testDirectory;

    @DynamicPropertySource
    private static void setRepositoryPath(DynamicPropertyRegistry registry) {
        registry.add("app.git.repositories", () -> testDirectory.toString());
    }

    @AfterEach
    public void cleanup() throws IOException {
        PathUtils.deleteFilesRecursively(testDirectory, true);
    }

    @Test
    public void testCreateConfiguration_shouldSucceed() {
        Assertions.assertThatCode(() -> configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME))
                .doesNotThrowAnyException();
    }

    @Test
    public void testCreateConfiguration_shouldCreateConfigurationDirectory() {
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(resolveTestConfigurationPath()).exists();
    }

    @Test
    public void testCreateConfiguration_configurationAlreadyExists_shouldThrowRepositoryAlreadyExistsException() {
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        Assertions.assertThatThrownBy(() -> configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME))
                .isInstanceOf(RepositoryAlreadyExistsException.class);
    }


    @Test
    public void testFindCurrentVersionOfConfigurationByName_nonExistingConfiguration_shouldReturnEmptyOptional() {
        var configuration = configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(configuration).isEmpty();
    }

    @Test
    public void testFindCurrentVersionOfConfigurationByName_existingConfigurationWithoutVersions_shouldReturnEmptyOptional() {
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var configuration = configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(configuration).isEmpty();
    }

    @Test
    public void testFindCurrentVersionOfConfigurationByName_existingConfigurationWithVersions_shouldReturnConfiguration() {
        var originalConfiguration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);

        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.saveConfiguration(originalConfiguration);

        var optionalConfiguration = configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(optionalConfiguration)
                .isPresent()
                .hasValueSatisfying(configuration -> ConfigurationAssert.assertThat(configuration)
                        .containsSameElementsAs(originalConfiguration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));
    }

    @Test
    public void testFindCurrentConfiguration_existingConfigurationWithMultipleVersions_shouldReturnLatestVersion() {
        var originalConfiguration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.saveConfiguration(originalConfiguration);

        var updatedConfiguration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 0, 0);
        configurationRepository.saveConfiguration(updatedConfiguration);

        var optionalConfiguration = configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(optionalConfiguration)
                .isPresent()
                .hasValueSatisfying(configuration -> ConfigurationAssert.assertThat(configuration)
                        .containsSameElementsAs(updatedConfiguration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));
    }

    @Test
    public void testFindSpecifiedVersionOfConfigurationByName_nonExistingConfiguration_shouldReturnEmptyOptional() {
        var configuration = configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);
        Assertions.assertThat(configuration).isEmpty();
    }

    @Test
    public void testFindSpecifiedVersionOfConfigurationByName_existingConfigurationWithoutVersions_shouldReturnEmptyOptional() {
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var configuration = configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);
        Assertions.assertThat(configuration).isEmpty();
    }

    @Test
    public void testFindSpecifiedVersionOfConfigurationByName_existingConfiguration_shouldReturnConfiguration() {
        var originalConfiguration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var originalSavedConfiguration = configurationRepository.saveConfiguration(originalConfiguration);

        var optionalConfiguration = configurationRepository.findSpecifiedVersionOfConfigurationByName(
                TEST_CONFIGURATION_NAME,
                Objects.requireNonNull(originalSavedConfiguration.getVersion())
        );

        Assertions.assertThat(optionalConfiguration)
                .isPresent()
                .hasValueSatisfying(configuration -> ConfigurationAssert.assertThat(configuration)
                        .containsSameElementsAs(originalSavedConfiguration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));
    }

    @Test
    public void testFindSpecifiedVersionOfConfigurationByName_existingConfigurationWithMultipleVersions_shouldReturnSpecifiedVersion() {
        var originalConfiguration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var originalSavedConfiguration = configurationRepository.saveConfiguration(originalConfiguration);

        var updatedConfiguration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 0, 0);
        var updatedSavedConfiguration = configurationRepository.saveConfiguration(updatedConfiguration);

        var optionalOriginalConfig = configurationRepository.findSpecifiedVersionOfConfigurationByName(
                TEST_CONFIGURATION_NAME,
                Objects.requireNonNull(originalSavedConfiguration.getVersion())
        );

        Assertions.assertThat(optionalOriginalConfig)
                .isPresent()
                .hasValueSatisfying(configuration -> ConfigurationAssert.assertThat(configuration)
                        .containsSameElementsAs(originalSavedConfiguration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));

        var optionalUpdatedConfig = configurationRepository.findSpecifiedVersionOfConfigurationByName(
                TEST_CONFIGURATION_NAME,
                Objects.requireNonNull(updatedSavedConfiguration.getVersion())
        );

        Assertions.assertThat(optionalUpdatedConfig)
                .isPresent()
                .hasValueSatisfying(configuration -> ConfigurationAssert.assertThat(configuration)
                        .containsSameElementsAs(updatedSavedConfiguration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));
    }

    @Test
    public void testFindAllConfigurations_noConfigurations_shouldReturnEmptyList() {
        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).isEmpty();
    }

    @Test
    public void testFindAllConfigurations_threeConfigurations_shouldReturnAllThreeConfigurations() {
        var testConfig1 = "testConfig1";
        var testConfig2 = "testConfig2";
        var testConfig3 = "testConfig3";

        configurationRepository.createConfiguration(testConfig1);
        configurationRepository.createConfiguration(testConfig2);
        configurationRepository.createConfiguration(testConfig3);

        var configuration1 = getEmptyConfiguration(testConfig1);
        var configuration2 = getEmptyConfiguration(testConfig2);
        var configuration3 = getEmptyConfiguration(testConfig3);

        configurationRepository.saveConfiguration(configuration1);
        configurationRepository.saveConfiguration(configuration2);
        configurationRepository.saveConfiguration(configuration3);

        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).satisfiesExactlyInAnyOrder(
                configuration -> ConfigurationAssert.assertThat(configuration).hasName(testConfig1).hasValidVersion().containsSameElementsAs(configuration1),
                configuration -> ConfigurationAssert.assertThat(configuration).hasName(testConfig2).hasValidVersion().containsSameElementsAs(configuration2),
                configuration -> ConfigurationAssert.assertThat(configuration).hasName(testConfig3).hasValidVersion().containsSameElementsAs(configuration3)
        );
    }

    @Test
    public void testFindAllConfigurations_threeConfigurations_oneWithoutVersion_shouldReturnTwoConfigurations() {
        var testConfig1 = "testConfig1";
        var testConfig2 = "testConfig2";
        var testConfig3 = "testConfig3";

        configurationRepository.createConfiguration(testConfig1);
        configurationRepository.createConfiguration(testConfig2);
        configurationRepository.createConfiguration(testConfig3);

        var configuration1 = getEmptyConfiguration(testConfig1);
        var configuration2 = getEmptyConfiguration(testConfig2);

        configurationRepository.saveConfiguration(configuration1);
        configurationRepository.saveConfiguration(configuration2);

        var configurations = configurationRepository.findAllConfigurations();
        Assertions.assertThat(configurations).satisfiesExactlyInAnyOrder(
                configuration -> ConfigurationAssert.assertThat(configuration).hasName(testConfig1).hasValidVersion().containsSameElementsAs(configuration1),
                configuration -> ConfigurationAssert.assertThat(configuration).hasName(testConfig2).hasValidVersion().containsSameElementsAs(configuration2)
        );
    }

    @Test
    public void testSaveConfiguration_nonExistingConfiguration_shouldThrowRepositoryDoesNotExistException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        Assertions.assertThatThrownBy(() -> configurationRepository.saveConfiguration(configuration))
                .isInstanceOf(RepositoryDoesNotExistException.class);
    }

    @Test
    public void testSaveConfiguration_emptyConfiguration_shouldSaveConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedConfiguration = configurationRepository.saveConfiguration(configuration);

        Assertions.assertThat(savedConfiguration)
                .isNotNull()
                .satisfies(config -> ConfigurationAssert.assertThat(config)
                        .containsSameElementsAs(configuration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));
    }

    @Test
    public void testSaveConfiguration_nonEmptyConfiguration_shouldSaveConfiguration() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedConfiguration = configurationRepository.saveConfiguration(configuration);

        Assertions.assertThat(savedConfiguration)
                .isNotNull()
                .satisfies(config -> ConfigurationAssert.assertThat(config)
                        .containsSameElementsAs(configuration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));
    }

    @Test
    public void testSaveConfiguration_largeConfiguration_shouldSaveConfiguration() {
        // not that large but AssertJ's recursive comparison is slow
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 2, 5, 4);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedConfiguration = configurationRepository.saveConfiguration(configuration);

        Assertions.assertThat(savedConfiguration)
                .isNotNull()
                .satisfies(config -> ConfigurationAssert.assertThat(config)
                        .containsSameElementsAs(configuration)
                        .hasValidVersion()
                        .hasName(TEST_CONFIGURATION_NAME));
    }

    @Test
    public void testDeleteConfiguration_existingConfiguration_shouldDeleteConfigurationDirectory() {
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.deleteConfiguration(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(resolveTestConfigurationPath()).doesNotExist();
    }

    @Test
    public void testDeleteConfiguration_existingConfiguration_shouldNotBeAbleToFindConfigurationAfterDeletion() {
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.saveConfiguration(DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1));

        Assertions.assertThat(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).isPresent();
        configurationRepository.deleteConfiguration(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).isEmpty();
    }

    @Test
    public void testDeleteConfiguration_existingConfiguration_shouldNotBeListedAfterDeletion() {
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.saveConfiguration(DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1));

        Assertions.assertThat(configurationRepository.findAllConfigurations()).isNotEmpty();
        configurationRepository.deleteConfiguration(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(configurationRepository.findAllConfigurations()).isEmpty();
    }

    @Test
    public void testDeleteConfiguration_nonExistingConfiguration_shouldNotThrowException() {
        Assertions.assertThatCode(() -> configurationRepository.deleteConfiguration(TEST_CONFIGURATION_NAME))
                .doesNotThrowAnyException();
    }

    @Test
    public void testCompareConfigurationVersions_nonExistingConfiguration_shouldThrowRepositoryDoesNotExistException () {
        Assertions.assertThatThrownBy(() -> configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, "v1.0.0", "v1.0.1", true))
                .isInstanceOf(RepositoryDoesNotExistException.class);
    }

    @Test
    public void testCompareConfigurationVersions_sameVersions_includeUnchangedFalse_shouldReturnEmptyDiff() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), savedVersion.getVersion(), false);
        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).isEmpty();
        Assertions.assertThat(comparison.getNodes()).isEmpty();
        Assertions.assertThat(comparison.getRelations()).isEmpty();
    }

    @Test
    public void testCompareConfigurationVersions_sameVersion_includeUnchangedTrue_shouldReturnAllDiffs() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), savedVersion.getVersion(), true);
        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).hasSize(1).allSatisfy(modelDiff ->
                Assertions.assertThat(modelDiff.getDiffType()).isEqualTo("UNCHANGED"));
        Assertions.assertThat(comparison.getNodes()).hasSize(2).allSatisfy(nodeDiff ->
                Assertions.assertThat(nodeDiff.getDiffType()).isEqualTo("UNCHANGED"));
        Assertions.assertThat(comparison.getRelations()).hasSize(2).allSatisfy(relationDiff ->
                Assertions.assertThat(relationDiff.getDiffType()).isEqualTo("UNCHANGED"));
    }

    @Test
    public void testCompareConfigurationVersions_unchangedVersion_includeUnchangedFalse_shouldReturnEmptyDiff() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);
        var updatedVersion = configurationRepository.saveConfiguration(configuration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), updatedVersion.getVersion(), false);
        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).isEmpty();
        Assertions.assertThat(comparison.getNodes()).isEmpty();
        Assertions.assertThat(comparison.getRelations()).isEmpty();
    }

    @Test
    public void testCompareConfigurationVersions_unchangedVersion_includeUnchangedTrue_shouldReturnAllDiffs() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);
        var updatedVersion = configurationRepository.saveConfiguration(configuration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), updatedVersion.getVersion(), true);
        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).hasSize(1).allSatisfy(modelDiff ->
                Assertions.assertThat(modelDiff.getDiffType()).isEqualTo("UNCHANGED"));
        Assertions.assertThat(comparison.getNodes()).hasSize(2).allSatisfy(nodeDiff ->
                Assertions.assertThat(nodeDiff.getDiffType()).isEqualTo("UNCHANGED"));
        Assertions.assertThat(comparison.getRelations()).hasSize(2).allSatisfy(relationDiff ->
                Assertions.assertThat(relationDiff.getDiffType()).isEqualTo("UNCHANGED"));
    }

    @Test
    public void testCompareConfigurationVersions_addedElements_shouldReturnAddDiffs() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);

        var updatedConfiguration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        updatedConfiguration.setModels(Set.of(
                DomainModelGen.generateRandomizedModel(2, 1),
                DomainModelGen.generateRandomizedModel(3, 0)
        ));
        var updatedVersion = configurationRepository.saveConfiguration(updatedConfiguration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), updatedVersion.getVersion(), false);
        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).hasSize(2).allSatisfy(modelDiff ->
                Assertions.assertThat(modelDiff.getDiffType()).isEqualTo("ADD"));
        Assertions.assertThat(comparison.getNodes()).hasSize(5).allSatisfy(nodeDiff ->
                Assertions.assertThat(nodeDiff.getDiffType()).isEqualTo("ADD"));
        Assertions.assertThat(comparison.getRelations()).hasSize(2).allSatisfy(relationDiff ->
                Assertions.assertThat(relationDiff.getDiffType()).isEqualTo("ADD"));
    }

    @Test
    public void testCompareConfigurationVersions_deletedElements_shouldReturnDeletedDiffs() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);

        var updatedVersion = configurationRepository.saveConfiguration(getEmptyConfiguration(TEST_CONFIGURATION_NAME));

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), updatedVersion.getVersion(), false);
        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).hasSize(1).allSatisfy(modelDiff ->
                Assertions.assertThat(modelDiff.getDiffType()).isEqualTo("DELETE"));
        Assertions.assertThat(comparison.getNodes()).hasSize(2).allSatisfy(nodeDiff ->
                Assertions.assertThat(nodeDiff.getDiffType()).isEqualTo("DELETE"));
        Assertions.assertThat(comparison.getRelations()).hasSize(2).allSatisfy(relationDiff ->
                Assertions.assertThat(relationDiff.getDiffType()).isEqualTo("DELETE"));
    }

    @Test
    public void testCompareConfigurationVersions_modifiedElements_shouldReturnModifiedDiffs() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);

        var configurationProcessor = new ConfigurationProcessor(configuration);
        configurationProcessor.processModels(model -> model.setTitle(model.getTitle() + "-updated"));
        configurationProcessor.processNodes((node, model) -> node.setTitle(node.getTitle() + "-updated"));
        configurationProcessor.processRelations((relation, node) -> relation.setTitle(relation.getTitle() + "-updated"));

        var updatedVersion = configurationRepository.saveConfiguration(configuration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), updatedVersion.getVersion(), false);
        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).hasSize(1).allSatisfy(modelDiff ->
                Assertions.assertThat(modelDiff.getDiffType()).isEqualTo("MODIFY"));
        Assertions.assertThat(comparison.getNodes()).hasSize(2).allSatisfy(nodeDiff ->
                Assertions.assertThat(nodeDiff.getDiffType()).isEqualTo("MODIFY"));
        Assertions.assertThat(comparison.getRelations()).hasSize(2).allSatisfy(relationDiff ->
                Assertions.assertThat(relationDiff.getDiffType()).isEqualTo("MODIFY"));
    }

    @Test
    public void testCompareConfigurations_multipleDifferences_shouldReturnCorrectDiffTypes() {
        var configuration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);

        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);

        var configurationProcessor = new ConfigurationProcessor(configuration);
        configurationProcessor.processNodes((node, model) -> {
            node.setRelations(Collections.emptySet());
            node.setTitle(node.getTitle() + "-updated");
        });
        configurationProcessor.processModels(model -> model.getNodes().add(DomainModelGen.generateRandomizedNode()));

        var updatedVersion = configurationRepository.saveConfiguration(configuration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), updatedVersion.getVersion(), true);

        // Overall
        // Removed two relations
        // added a node
        // updated two nodes
        // left one model unchanged

        Assertions.assertThat(comparison).isNotNull();
        Assertions.assertThat(comparison.getModels()).hasSize(1).allSatisfy(modelDiff ->
                Assertions.assertThat(modelDiff.getDiffType()).isEqualTo("UNCHANGED"));

        Assertions.assertThat(comparison.getNodes()).satisfiesOnlyOnce(nodeDiff ->
                Assertions.assertThat(nodeDiff.getDiffType()).isEqualTo("ADD"));
        Assertions.assertThat(comparison.getNodes()).filteredOn(nodeDiff -> nodeDiff.getDiffType().equals("MODIFY")).hasSize(2);

        Assertions.assertThat(comparison.getRelations()).hasSize(2).allSatisfy(relationDiff ->
                Assertions.assertThat(relationDiff.getDiffType()).isEqualTo("DELETE"));
    }

    @Test
    public void testCompareConfigurationVersions_addedElements_metadataShouldBeRemoved() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configurationRepository.createConfiguration(TEST_CONFIGURATION_NAME);
        var savedVersion = configurationRepository.saveConfiguration(configuration);

        var updatedConfiguration = DomainModelGen.generateRandomizedConfiguration(TEST_CONFIGURATION_NAME, 1, 2, 1);
        var updatedVersion = configurationRepository.saveConfiguration(updatedConfiguration);

        @SuppressWarnings("ConstantConditions")
        var comparison = configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, savedVersion.getVersion(), updatedVersion.getVersion(), false);

        Assertions.assertThat(comparison).isNotNull();

        Assertions.assertThat(comparison.getModels())
                .extracting(ModelDiff::getContent)
                .noneMatch(modelDiff -> modelDiff.contains("metadata"));

        Assertions.assertThat(comparison.getNodes())
                .extracting(NodeDiff::getContent)
                .noneMatch(nodeDiff -> nodeDiff.contains("metadata"));


        Assertions.assertThat(comparison.getRelations())
                .extracting(RelationDiff::getContent)
                .noneMatch(relationDiff -> relationDiff.contains("metadata"));
    }


    private Path resolveTestConfigurationPath() {
        return resolveTestDirectoryPath(TEST_CONFIGURATION_NAME);
    }

    @SuppressWarnings("SameParameterValue")
    private Path resolveTestDirectoryPath(String path) {
        return testDirectory.resolve(path);
    }

    private Configuration getEmptyConfiguration(String name) {
        return getEmptyConfiguration(name, null);
    }

    @SuppressWarnings("SameParameterValue")
    private Configuration getEmptyConfiguration(String name, String version) {
        var configuration = new Configuration();
        configuration.setName(name);
        configuration.setVersion(version);
        return configuration;
    }
}
