package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import at.ac.tuwien.model.change.management.git.util.VersionControlUtils;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class RepositoryManagerTest {

    @TempDir
    private Path testDirectory;

    private RepositoryManager repositoryManager;

    @BeforeEach
    public void setUp() {
        var gitProperties = new GitProperties();
        gitProperties.setRepositories(testDirectory);
        repositoryManager = new RepositoryManagerImpl(gitProperties);
    }

    @Test
    public void testAccessRepository_nonExistingRepository_shouldSucceed() {
        var repositoryName = "test";
        try (var repository = repositoryManager.accessRepository(repositoryName)) {
            Assertions.assertThat(repository).isNotNull()
                    .extracting(RepositoryUtils::getRepositoryName)
                    .isEqualTo(repositoryName);
            Assertions.assertThat(RepositoryUtils.repositoryExists(repository)).isFalse();
        }
    }

    @Test
    public void testAccessRepository_existingRepository_shouldSucceed() throws GitAPIException {
        var repositoryName = "test";
        createRepository(repositoryName);
        try (var repository = repositoryManager.accessRepository(repositoryName)) {
            Assertions.assertThat(repository)
                    .isNotNull()
                    .extracting(RepositoryUtils::getRepositoryName)
                    .isEqualTo(repositoryName);
            Assertions.assertThat(RepositoryUtils.repositoryExists(repository)).isTrue();
        }
    }

    @Test
    public void testListRepositories_noRepositories_shouldReturnEmptyList() {
        Assertions.assertThat(repositoryManager.listRepositories()).isEmpty();
    }

    @Test
    public void testListRepositories_oneRepository_shouldReturnListWithOneRepository() throws GitAPIException {
        var repositoryName = "test";
        createRepository(repositoryName);
        Assertions.assertThat(repositoryManager.listRepositories()).hasSize(1)
                .map(RepositoryUtils::getRepositoryName)
                .containsExactly(repositoryName);
    }

    @Test
    public void testListRepositories_threeRepositories_shouldReturnListWithThreeRepositories() throws GitAPIException {
        var repositoryName1 = "test1";
        var repositoryName2 = "test2";
        var repositoryName3 = "test3";
        createRepository(repositoryName1);
        createRepository(repositoryName2);
        createRepository(repositoryName3);
        Assertions.assertThat(repositoryManager.listRepositories())
                .hasSize(3)
                .map(RepositoryUtils::getRepositoryName)
                .containsExactlyInAnyOrder(repositoryName1, repositoryName2, repositoryName3);
    }


    private void createRepository(String repositoryName) throws GitAPIException {
        try (var repository = repositoryManager.accessRepository(repositoryName)) {
            VersionControlUtils.initRepository(repository);
        }
    }
}
