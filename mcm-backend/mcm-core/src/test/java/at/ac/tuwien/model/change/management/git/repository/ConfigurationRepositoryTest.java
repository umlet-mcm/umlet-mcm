package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.util.RepositoryManager;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationRepositoryTest {

    @TempDir
    private Path tempDir;
    private RepositoryManager repositoryManager;
    private ConfigurationRepository configurationRepository;


    @BeforeEach
    public void setUp() {
        var gitProperties = mock(GitProperties.class);
        when(gitProperties.getRepositoryPath()).thenReturn(tempDir);
//        repositoryManager = new RepositoryManager(gitProperties);
        configurationRepository = new ConfigurationRepositoryImpl(repositoryManager);
    }

    @Test
    public void testCreate_shouldCreateGitRepository() {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        configurationRepository.create(configuration);

        assertThat(repositoryManager.withRepository(configurationName, true, RepositoryUtils::repositoryExists)).isTrue();
        assertThat(tempDir.resolve("test").resolve(".git")).exists();
    }

    @Test
    public void testCreate_createTwoConfigurationsWithSameName_shouldThrowException() {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        configurationRepository.create(configuration);

        assertThatThrownBy(() -> configurationRepository.create(configuration))
                .isInstanceOf(ConfigurationAlreadyExistsException.class)
                .hasMessage("Could not create configuration 'test', because it already exists");
    }

    @Test
    public void testDelete_shouldDeleteGitRepository() {
        var configurationName = "test";
        var configuration = new Configuration();
        configuration.setName(configurationName);
        configurationRepository.create(configuration);
        configurationRepository.delete(configurationName);
        assertThat(repositoryManager.withRepository(configurationName, false, RepositoryUtils::repositoryExists)).isFalse();
        assertThat(tempDir.resolve("test")).doesNotExist();
    }
}
