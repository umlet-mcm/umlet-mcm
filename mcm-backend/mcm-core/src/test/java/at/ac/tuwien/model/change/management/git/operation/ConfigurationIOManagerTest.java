package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import at.ac.tuwien.model.change.management.git.util.VersionControlUtils;
import at.ac.tuwien.model.change.management.testutils.model.DomainModelGen;
import at.ac.tuwien.model.change.management.testutils.git.MockDSLTransformer;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigurationIOManagerTest {

    @TempDir
    private Path testDirectory;
    private ConfigurationIOManager configurationIOManager;

    @BeforeEach
    public void setUp() {
        var dslTransformer = new MockDSLTransformer();
        configurationIOManager = new ConfigurationIOManagerImpl(dslTransformer);
    }

    @Test
    public void testWrite_emptyConfiguration_shouldSucceed() throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        try (var repository = createRepository(configurationName)) {
            Assertions.assertThatCode(() -> configurationIOManager.writeConfigurationToRepository(repository, configuration))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testWrite_nonEmptyConfiguration_shouldSucceed() throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 0, 0, 0);
        configuration.setName(configurationName);
        try (var repository = createRepository(configurationName)) {
            Assertions.assertThatCode(() -> configurationIOManager.writeConfigurationToRepository(repository, configuration))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testWrite_fullConfiguration_shouldSucceed() throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 5, 5, 5);
        configuration.setName(configurationName);
        try (var repository = createRepository(configurationName)) {
            Assertions.assertThatCode(() -> configurationIOManager.writeConfigurationToRepository(repository, configuration))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testWriteRead_emptyConfiguration_writtenAndReadConfigurationsShouldBeEqual()
            throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        try (var repository = createRepository(configurationName)) {
            saveConfigurationToRepository(repository, configuration);
            var readConfiguration = configurationIOManager.readConfigurationFromRepository(repository, Constants.HEAD);
            Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
            Assertions.assertThat(readConfiguration)
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(configuration);
        }
    }

    @Test
    public void testWriteRead_configurationWithoutNodes_writtenAndReadConfigurationsShouldBeEqual()
            throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 1, 0, 0);
        try (var repository = createRepository(configurationName)) {
            saveConfigurationToRepository(repository, configuration);
            var readConfiguration = configurationIOManager.readConfigurationFromRepository(repository, Constants.HEAD);
            Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
            Assertions.assertThat(readConfiguration)
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(configuration);
        }
    }

    @Test
    public void testReadWrite_configurationWithoutRelations_writtenAndReadConfigurationsShouldBeEqual()
            throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 1, 1, 0);
        try (var repository = createRepository(configurationName)) {
            saveConfigurationToRepository(repository, configuration);
            var readConfiguration = configurationIOManager.readConfigurationFromRepository(repository, Constants.HEAD);
            Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
            Assertions.assertThat(readConfiguration)
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(configuration);
        }
    }

    @Test
    public void testReadWrite_fullConfiguration_writtenAndReadConfigurationsShouldBeEqual()
            throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 1, 1, 1);
        try (var repository = createRepository(configurationName)) {
            saveConfigurationToRepository(repository, configuration);
            var readConfiguration = configurationIOManager.readConfigurationFromRepository(repository, Constants.HEAD);
            Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
            Assertions.assertThat(readConfiguration)
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(configuration);
        }
    }

    @Test
    public void testReadWrite_largeConfiguration_writtenAndReadConfigurationsShouldBeEqual()
            throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 5, 5, 5);
        try (var repository = createRepository(configurationName)) {
            saveConfigurationToRepository(repository, configuration);
            var readConfiguration = configurationIOManager.readConfigurationFromRepository(repository, Constants.HEAD);
            Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
            Assertions.assertThat(readConfiguration)
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(configuration);
        }
    }

    @Test
    public void testClear_emptyRepository_shouldSucceed() throws GitAPIException, IOException {
        var configurationName = "test";
        try (var repository = createRepository(configurationName)) {
            Assertions.assertThatCode(() -> configurationIOManager.clearConfigurationRepository(repository))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testClear_nonEmptyRepository_shouldSucceed() throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 1, 1, 1);
        try (var repository = createRepository(configurationName)) {
            saveConfigurationToRepository(repository, configuration);
            Assertions.assertThatCode(() -> configurationIOManager.clearConfigurationRepository(repository))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testClear_fullRepository_shouldClearRepository() throws GitAPIException, IOException {
        var configurationName = "test";
        var configuration = DomainModelGen.generateRandomizedConfiguration(configurationName, 5, 5, 5);
        try (var repository = createRepository(configurationName)) {
            saveConfigurationToRepository(repository, configuration);
            configurationIOManager.clearConfigurationRepository(repository);
            commitRepository(repository);
            var readConfiguration = configurationIOManager.readConfigurationFromRepository(repository, Constants.HEAD);
            Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
            Assertions.assertThat(readConfiguration.getModels()).isNull();
        }
    }

    private Repository createRepository(String repositoryName) throws IOException, GitAPIException {
        var repositoryDir = testDirectory.resolve(repositoryName);
        var repository = RepositoryUtils.getRepositoryAtPath(repositoryDir);
        VersionControlUtils.initRepository(repository);
        return repository;
    }

    private void saveConfigurationToRepository(Repository repository, Configuration configuration) throws GitAPIException {
        var contents = configurationIOManager.writeConfigurationToRepository(repository, configuration);
        VersionControlUtils.stageRepositoryContents(repository, contents);
        VersionControlUtils.commitRepository(repository, "Initial commit");
    }

    public void commitRepository(Repository repository) throws GitAPIException {
        VersionControlUtils.commitRepository(repository, "Initial commit");
    }
}
