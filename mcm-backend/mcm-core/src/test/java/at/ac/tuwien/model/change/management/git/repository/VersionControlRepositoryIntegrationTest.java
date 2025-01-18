package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryVersioningException;
import at.ac.tuwien.model.change.management.git.integration.GitTestConfig;
import at.ac.tuwien.model.change.management.git.util.PathUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
@ContextConfiguration(classes = GitTestConfig.class)
public class VersionControlRepositoryIntegrationTest {

    @Autowired
    private VersionControlRepository versionControlRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    private static final String TEST_REPOSITORY_NAME = "test-repository";

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
    public void testGetCurrentVersion_nonExistingRepository_shouldReturnEmptyOptional() {
        var version = versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME);
        Assertions.assertThat(version).isEmpty();
    }

    @Test
    public void testGetCurrentVersion_repositoryWithoutVersions_shouldReturnEmptyOptional() {
        initRepository(TEST_REPOSITORY_NAME);
        var version = versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME);
        Assertions.assertThat(version).isEmpty();
    }

    @Test
    public void testGetCurrentVersion_repositoryWithOneVersion_shouldReturnVersion() {
        initRepository(TEST_REPOSITORY_NAME);
        var commitHash = createCommit(TEST_REPOSITORY_NAME);
        var version = versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME);
        Assertions.assertThat(version).isPresent().contains(commitHash);
    }

    @Test
    public void testGetCurrentVersion_threeVersions_shouldReturnLatestVersion() {
        initRepository(TEST_REPOSITORY_NAME);
        createCommit(TEST_REPOSITORY_NAME);
        createCommit(TEST_REPOSITORY_NAME);
        var commit3 = createCommit(TEST_REPOSITORY_NAME);
        var version = versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME);
        Assertions.assertThat(version).isPresent().contains(commit3);
    }

    @Test
    public void testListVersions_nonExistingRepository_shouldReturnEmptyList() {
        var versions = versionControlRepository.listVersions(TEST_REPOSITORY_NAME);
        Assertions.assertThat(versions).isEmpty();
    }

    @Test
    public void testListVersions_repositoryWithoutVersions_shouldReturnEmptyList() {
        initRepository(TEST_REPOSITORY_NAME);
        var versions = versionControlRepository.listVersions(TEST_REPOSITORY_NAME);
        Assertions.assertThat(versions).isEmpty();
    }

    @Test
    public void testListVersions_repositoryWithOneVersion_shouldReturnOneVersion() {
        initRepository(TEST_REPOSITORY_NAME);
        var commitHash = createCommit(TEST_REPOSITORY_NAME);
        var versions = versionControlRepository.listVersions(TEST_REPOSITORY_NAME);
        Assertions.assertThat(versions).containsExactly(commitHash);
    }

    @Test
    public void testListVersions_threeVersions_shouldReturnAllVersions() {
        initRepository(TEST_REPOSITORY_NAME);
        var commit1 = createCommit(TEST_REPOSITORY_NAME);
        var commit2 = createCommit(TEST_REPOSITORY_NAME);
        var commit3 = createCommit(TEST_REPOSITORY_NAME);
        var versions = versionControlRepository.listVersions(TEST_REPOSITORY_NAME);
        Assertions.assertThat(versions).containsExactly(commit3, commit2, commit1);
    }

    @Test
    public void testCheckoutVersion_nonExistingRepository_shouldThrowRepositoryDoesNotExistException() {
        Assertions.assertThatThrownBy(() -> versionControlRepository.checkoutVersion(TEST_REPOSITORY_NAME, "version"))
                .isInstanceOf(RepositoryDoesNotExistException.class);
    }

    @Test
    public void testCheckoutVersion_existingRepositoryWithoutVersions_shouldThrowRepositoryVersioningException() {
        initRepository(TEST_REPOSITORY_NAME);
        Assertions.assertThatThrownBy(() -> versionControlRepository.checkoutVersion(TEST_REPOSITORY_NAME, "version"))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCheckoutVersion_nonExistingVersion_shouldThrowRepositoryVersioningException() {
        initRepository(TEST_REPOSITORY_NAME);
        var version = createCommit(TEST_REPOSITORY_NAME);
        Assertions.assertThatThrownBy(() -> versionControlRepository.checkoutVersion(TEST_REPOSITORY_NAME, version + "-non-existing-version"))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCheckoutVersion_existingRepositoryWithOneVersion_shouldCheckoutVersion() {
        initRepository(TEST_REPOSITORY_NAME);
        var commitHash = createCommit(TEST_REPOSITORY_NAME);
        versionControlRepository.checkoutVersion(TEST_REPOSITORY_NAME, commitHash);
        Assertions.assertThat(versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME))
                .contains(commitHash);
    }

    @Test
    public void testCheckoutVersions_multipleVersions_shouldCheckoutCorrectVersion() {
        initRepository(TEST_REPOSITORY_NAME);
        createCommit(TEST_REPOSITORY_NAME);
        var commit2 = createCommit(TEST_REPOSITORY_NAME);
        var commit3 = createCommit(TEST_REPOSITORY_NAME);

        Assertions.assertThat(versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME))
                .contains(commit3);
        versionControlRepository.checkoutVersion(TEST_REPOSITORY_NAME, commit2);
        Assertions.assertThat(versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME))
                .contains(commit2);
    }

    @Test
    public void testCheckoutVersions_thenListVersions_checkoutShouldNotInfluenceVersionsList() {
        initRepository(TEST_REPOSITORY_NAME);
        var commit1 = createCommit(TEST_REPOSITORY_NAME);
        var commit2 = createCommit(TEST_REPOSITORY_NAME);
        var commit3 = createCommit(TEST_REPOSITORY_NAME);

        versionControlRepository.checkoutVersion(TEST_REPOSITORY_NAME, commit2);
        var versions = versionControlRepository.listVersions(TEST_REPOSITORY_NAME);
        Assertions.assertThat(versions).containsExactly(commit3, commit2, commit1);
    }

    @Test
    public void testResetToVersion_nonExistingRepository_shouldThrowRepositoryDoesNotExistException() {
        Assertions.assertThatThrownBy(() -> versionControlRepository.resetToVersion(TEST_REPOSITORY_NAME, "version"))
                .isInstanceOf(RepositoryDoesNotExistException.class);
    }

    @Test
    public void testResetToVersion_existingRepositoryWithoutVersions_shouldThrowRepositoryVersioningException() {
        initRepository(TEST_REPOSITORY_NAME);
        Assertions.assertThatThrownBy(() -> versionControlRepository.resetToVersion(TEST_REPOSITORY_NAME, "version"))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testResetToVersion_nonExistingVersion_shouldThrowRepositoryVersioningException() {
        initRepository(TEST_REPOSITORY_NAME);
        var version = createCommit(TEST_REPOSITORY_NAME);
        Assertions.assertThatThrownBy(() -> versionControlRepository.resetToVersion(TEST_REPOSITORY_NAME, version + "-non-existing-version"))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testResetToVersion_existingRepositoryWithOneVersion_shouldResetToVersion() {
        initRepository(TEST_REPOSITORY_NAME);
        var commitHash = createCommit(TEST_REPOSITORY_NAME);
        versionControlRepository.resetToVersion(TEST_REPOSITORY_NAME, commitHash);
        Assertions.assertThat(versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME))
                .contains(commitHash);
    }

    @Test
    public void testResetToVersion_multipleVersions_shouldResetToCorrectVersion() {
        initRepository(TEST_REPOSITORY_NAME);
        createCommit(TEST_REPOSITORY_NAME);
        var commit2 = createCommit(TEST_REPOSITORY_NAME);
        var commit3 = createCommit(TEST_REPOSITORY_NAME);

        Assertions.assertThat(versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME))
                .contains(commit3);
        versionControlRepository.resetToVersion(TEST_REPOSITORY_NAME, commit2);
        Assertions.assertThat(versionControlRepository.getCurrentVersion(TEST_REPOSITORY_NAME))
                .contains(commit2);
    }

    @Test
    public void testResetToVersion_thenListVersions_resetShouldInfluenceVersionsList() {
        initRepository(TEST_REPOSITORY_NAME);
        var commit1 = createCommit(TEST_REPOSITORY_NAME);
        var commit2 = createCommit(TEST_REPOSITORY_NAME);
        var commit3 = createCommit(TEST_REPOSITORY_NAME);

        Assertions.assertThat(versionControlRepository.listVersions(TEST_REPOSITORY_NAME))
                        .containsExactly(commit3, commit2, commit1);

        versionControlRepository.resetToVersion(TEST_REPOSITORY_NAME, commit2);

        Assertions.assertThat(versionControlRepository.listVersions(TEST_REPOSITORY_NAME))
                .containsExactly(commit2, commit1);
    }

    @SuppressWarnings("SameParameterValue")
    private void initRepository(String repositoryName) {
        configurationRepository.createConfiguration(repositoryName);
    }

    @SuppressWarnings("SameParameterValue")
    private String createCommit(String repositoryName) {
        var configuration = new Configuration();
        configuration.setName(repositoryName);
        return configurationRepository.saveConfiguration(configuration).getVersion();
    }
}
