package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDeleteException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryWriteException;
import at.ac.tuwien.model.change.management.git.util.PathUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagedRepositoryTest {

    @TempDir
    private Path tempDir;

    @Mock
    private ManagedRepositoryVersioning mockVersioning;

    private final String testName = "testRepository";

    private Repository jGitRepository;

    private ManagedRepository testRepository;

    @BeforeEach
    public void setup() {
        jGitRepository = getjGitRepository();
        testRepository = getManagedRepository(jGitRepository, mockVersioning);
    }

    @AfterEach
    public void cleanup() {
        testRepository.close();
        jGitRepository.close();
    }

    @Test
    public void testExists_repositoryInitialized_returnTrue() {
        initTestRepository();
        when(mockVersioning.isInitialized()).thenReturn(true);
        Assertions.assertThat(testRepository.exists()).isTrue();
    }

    @Test
    public void testExists_repositoryNotInitialized_returnFalse() {
        when(mockVersioning.isInitialized()).thenReturn(false);
        Assertions.assertThat(testRepository.exists()).isFalse();
    }

    @Test
    public void testWriteRepositoryFiles_noFiles_returnEmptySet() {
        initTestRepository();
        List<ManagedRepositoryFile> files = Collections.emptyList();
        Assertions.assertThat(testRepository.writeRepositoryFiles(files)).isEmpty();
    }

    @Test
    public void testWriteRepositoryFiles_oneFile_shouldReturnFilePath() {
        initTestRepository();
        var testPath = Path.of("test");
        var testContent = "test";
        var files = List.of(createManagedRepositoryFile(testPath, testContent));

        var result = testRepository.writeRepositoryFiles(files);
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst())
                .endsWith(testPath)
                .hasContent(testContent);
    }

    @Test
    public void testWriteRepositoryFiles_twoFiles_shouldReturnFilePaths() {
        initTestRepository();
        var fileOne = createManagedRepositoryFile(Path.of("test1"), "test1");
        var fileTwo = createManagedRepositoryFile(Path.of("test2"), "test2");
        var files = List.of(fileOne, fileTwo);

        var result = testRepository.writeRepositoryFiles(files);
        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.getFirst())
                .endsWith(fileOne.path())
                .hasContent(fileOne.content());
        Assertions.assertThat(result.getLast())
                .endsWith(fileTwo.path())
                .hasContent(fileTwo.content());
    }

    @Test
    public void testWriteRepositoryFiles_repositoryNotInitialized_shouldStillWriteFiles() {
        var testPath = Path.of("test");
        var testContent = "test";
        var files = List.of(createManagedRepositoryFile(testPath, testContent));

        var result = testRepository.writeRepositoryFiles(files);
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.getFirst())
                .endsWith(testPath)
                .hasContent(testContent);
    }

    @Test
    public void testWriteRepositoryFiles_fileOutsideRepository_shouldThrowRepositoryAccessException() {
        initTestRepository();
        var files = List.of(createManagedRepositoryFile(Path.of("../test"), "test"));

        Assertions.assertThatThrownBy(() -> testRepository.writeRepositoryFiles(files))
                .isInstanceOf(RepositoryAccessException.class);
    }

    @Test
    public void testWriteRepositoryFiles_createDirectoriesThrowsIOException_shouldThrowRepositoryWriteException() {
        initTestRepository();
        try (var filesMock = mockStatic(Files.class)) {
            var files = List.of(createManagedRepositoryFile(Path.of("test"), "test"));
            filesMock.when(() -> Files.createDirectories(getWorkTree()))
                    .thenThrow(IOException.class);
            Assertions.assertThatThrownBy(() -> testRepository.writeRepositoryFiles(files))
                    .isInstanceOf(RepositoryWriteException.class);
        }
    }

    @Test
    public void testWriteRepositoryFiles_writingThrowsIOException_shouldThrowRepositoryWriteException() {
        initTestRepository();
        try (var filesMock = mockStatic(Files.class)) {
            var files = List.of(createManagedRepositoryFile(Path.of("test"), "test"));
            filesMock.when(() -> Files.writeString(
                    getWorkTree().resolve(files.getFirst().path()),
                    files.getFirst().content(),
                    testRepository.getEncoding()
            )).thenThrow(IOException.class);
            Assertions.assertThatThrownBy(() -> testRepository.writeRepositoryFiles(files))
                    .isInstanceOf(RepositoryWriteException.class);
        }
    }

    @Test
    public void testDeleteRepositoryFiles_noFiles_shouldNotThrowException() {
        initTestRepository();
        Assertions.assertThatCode(testRepository::deleteRepositoryFiles).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteRepositoryFiles_oneFile_shouldDeleteFile() {
        initTestRepository();
        var testPath = Path.of("test");
        var testContent = "test";
        var files = List.of(createManagedRepositoryFile(testPath, testContent));

        testRepository.writeRepositoryFiles(files);
        Assertions.assertThat(getWorkTree().resolve(testPath)).exists();

        testRepository.deleteRepositoryFiles(testPath);
        Assertions.assertThat(getWorkTree().resolve(testPath)).doesNotExist();
    }

    @Test
    public void testDeleteRepositoryFiles_twoFiles_shouldDeleteFiles() {
        initTestRepository();
        var fileOne = createManagedRepositoryFile(Path.of("test1"), "test1");
        var fileTwo = createManagedRepositoryFile(Path.of("test2"), "test2");
        var files = List.of(fileOne, fileTwo);

        testRepository.writeRepositoryFiles(files);
        Assertions.assertThat(getWorkTree().resolve(fileOne.path())).exists();
        Assertions.assertThat(getWorkTree().resolve(fileTwo.path())).exists();

        testRepository.deleteRepositoryFiles(fileOne.path(), fileTwo.path());
        Assertions.assertThat(getWorkTree().resolve(fileOne.path())).doesNotExist();
        Assertions.assertThat(getWorkTree().resolve(fileTwo.path())).doesNotExist();
    }

    @Test
    public void testDeleteRepositoryFiles_directory_shouldDeleteDirectory() {
        initTestRepository();
        var directory = "testDir";
        var files = List.of(createManagedRepositoryFile(Path.of(directory, "test1"), "test"));
        testRepository.writeRepositoryFiles(files);

        Assertions.assertThat(files).allSatisfy(file -> {
            var path = getWorkTree().resolve(file.path());
            Assertions.assertThat(path).exists();
        });

        testRepository.deleteRepositoryFiles(Path.of(directory));
        Assertions.assertThat(getWorkTree().resolve(directory)).doesNotExist();
        Assertions.assertThat(files).noneSatisfy(file -> {
            var path = getWorkTree().resolve(file.path());
            Assertions.assertThat(path).exists();
        });
    }

    @Test
    public void testDeleteRepositoryFiles_fileOutsideRepository_shouldThrowRepositoryAccessException() {
        initTestRepository();
        Assertions.assertThatThrownBy(() -> testRepository.deleteRepositoryFiles(Path.of("../test")))
                .isInstanceOf(RepositoryAccessException.class);
    }

    @Test
    public void testDeleteRepositoryFiles_deleteFilesThrowsIOException_shouldThrowRepositoryDeleteException() {
        initTestRepository();
        try (var pathUtilsMock = mockStatic(PathUtils.class)) {
            var file = new ManagedRepositoryFile(Path.of("test"), "test");
            testRepository.writeRepositoryFiles(List.of(file));
            pathUtilsMock.when(() -> PathUtils.deleteFilesRecursively(getWorkTree().resolve(file.path())))
                    .thenThrow(IOException.class);
            Assertions.assertThatThrownBy(() -> testRepository.deleteRepositoryFiles(file.path()))
                    .isInstanceOf(RepositoryDeleteException.class);
        }
    }


    @Test
    public void testGetRepositoryVersion_versionDoesNotExist_shouldReturnEmptyOptional() {
        initTestRepository();
        var version = testRepository.getRepositoryVersion("nonExistentVersion");
        Assertions.assertThat(version).isEmpty();
    }

    @Test
    public void testGetRepositoryVersion_versionExists_shouldReturnVersion() {
        initTestRepository();
        when(mockVersioning.isInitialized()).thenReturn(true);

        var versionID = commitTestRepository("Initial commit");
        var optionalVersion = testRepository.getRepositoryVersion(versionID);

        Assertions.assertThat(optionalVersion).hasValueSatisfying(version -> {
            Assertions.assertThat(version.id()).isEqualTo(versionID);
            Assertions.assertThat(version.objects()).isEmpty();
        });
    }

    @Test
    public void testGetRepositoryVersion_versionWithFile_shouldReturnVersionWithObject() {
        initTestRepository();
        when(mockVersioning.isInitialized()).thenReturn(true);

        var files = List.of(createManagedRepositoryFile(Path.of("test"), "test"));
        testRepository.writeRepositoryFiles(files);
        var versionID = commitTestRepository("Initial commit");
        var optionalVersion = testRepository.getRepositoryVersion(versionID);

        Assertions.assertThat(optionalVersion).hasValueSatisfying(version -> {
            Assertions.assertThat(version.id()).isEqualTo(versionID);
            Assertions.assertThat(version.objects()).hasSize(1);
        });
    }

    @Test
    public void testGetRepositoryVersion_versionWithTwoFiles_shouldReturnVersionWithTwoObjects() {
        initTestRepository();
        when(mockVersioning.isInitialized()).thenReturn(true);

        testRepository.writeRepositoryFiles(List.of(
                createManagedRepositoryFile(Path.of("test1"), "test1"),
                createManagedRepositoryFile(Path.of("test2"), "test2")
        ));
        var versionID = commitTestRepository("Initial commit");
        var optionalVersion = testRepository.getRepositoryVersion(versionID);

        Assertions.assertThat(optionalVersion).hasValueSatisfying(version -> {
            Assertions.assertThat(version.id()).isEqualTo(versionID);
            Assertions.assertThat(version.objects()).hasSize(2);
        });
    }

    @Test
    public void testGetRepositoryVersion_twoVersions_shouldReturnBothVersions() {
        initTestRepository();
        when(mockVersioning.isInitialized()).thenReturn(true);

        var files = List.of(createManagedRepositoryFile(Path.of("test1"), "test1"));
        testRepository.writeRepositoryFiles(files);
        var firstVersionID = commitTestRepository("Initial commit");
        testRepository.writeRepositoryFiles(List.of(createManagedRepositoryFile(Path.of("test2"), "test2")));
        var secondVersionID = commitTestRepository("Second commit");

        var optionalFirstVersion = testRepository.getRepositoryVersion(firstVersionID);
        var optionalSecondVersion = testRepository.getRepositoryVersion(secondVersionID);

        Assertions.assertThat(optionalFirstVersion).hasValueSatisfying(version -> {
            Assertions.assertThat(version.id()).isEqualTo(firstVersionID);
            Assertions.assertThat(version.objects()).hasSize(1);
        });
        Assertions.assertThat(optionalSecondVersion).hasValueSatisfying(version -> {
            Assertions.assertThat(version.id()).isEqualTo(secondVersionID);
            Assertions.assertThat(version.objects()).hasSize(2);
        });
    }

    @Test
    public void testGetRepository_repositoryNotInitialized_shouldReturnEmptyOptional() {
        when(mockVersioning.isInitialized()).thenReturn(false);
        var version = testRepository.getRepositoryVersion("nonExistentVersion");
        Assertions.assertThat(version).isEmpty();
    }

    @Test
    public void testGetCurrentRepositoryVersion_twoVersions_shouldReturnNewerVersion() {
        initTestRepository();
        when(mockVersioning.isInitialized()).thenReturn(true);

        testRepository.writeRepositoryFiles(List.of(createManagedRepositoryFile(Path.of("test1"), "test1")));
        commitTestRepository("Initial commit");
        testRepository.writeRepositoryFiles(List.of(createManagedRepositoryFile(Path.of("test2"), "test2")));
        var secondVersionID = commitTestRepository("Second commit");

        var secondVersion = testRepository.getRepositoryVersion(secondVersionID);
        var currentVersion = testRepository.getCurrentRepositoryVersion();

        Assertions.assertThat(currentVersion).usingRecursiveComparison().isEqualTo(secondVersion);
        Assertions.assertThat(currentVersion).hasValueSatisfying(v -> {
            Assertions.assertThat(v.id()).isEqualTo(secondVersionID);
            Assertions.assertThat(v.objects()).hasSize(2);
        });
    }

    @Test
    public void testGetCurrentRepositoryVersion_noVersions_shouldReturnEmptyOptional() {
        initTestRepository();
        var currentVersion = testRepository.getCurrentRepositoryVersion();
        Assertions.assertThat(currentVersion).isEmpty();
    }

    @Test
    public void testGetCurrentRepositoryVersion_repositoryNotInitialized_shouldReturnEmptyOptional() {
        when(mockVersioning.isInitialized()).thenReturn(false);
        var currentVersion = testRepository.getCurrentRepositoryVersion();
        Assertions.assertThat(currentVersion).isEmpty();
    }

    @Test
    public void testDeleteRepository_repositoryExists_shouldDeleteRepository() {
        initTestRepository();
        Assertions.assertThat(getWorkTree()).exists();
        testRepository.deleteRepository();
        Assertions.assertThat(getWorkTree()).doesNotExist();
        Assertions.assertThat(repositoryExists()).isFalse();
    }

    @Test
    public void testDeleteRepository_repositoryNotExists_shouldNotThrowException() {
        Assertions.assertThatCode(testRepository::deleteRepository).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteRepository_repositoryWithFiles_shouldDeleteAllFiles() {
        initTestRepository();
        var file = createManagedRepositoryFile(Path.of("test"), "test");
        testRepository.writeRepositoryFiles(List.of(file));
        Assertions.assertThat(getWorkTree()).exists();
        testRepository.deleteRepository();
        Assertions.assertThat(getWorkTree()).doesNotExist();
        Assertions.assertThat(repositoryExists()).isFalse();
        Assertions.assertThat(getWorkTree().resolve(file.path())).doesNotExist();
    }


    @Test
    public void testClose_repositoryNotClosed_shouldCloseRepository() {
        var spyJGitRepository = spy(getjGitRepository());
        var testRepositoryWithSpy = getManagedRepository(spyJGitRepository, mockVersioning);
        testRepositoryWithSpy.close();
        verify(spyJGitRepository).close();
    }

    @Test
    public void testClose_repositoryClosedTwice_jGitRepositoryClosedOnce() {
        var spyJGitRepository = spy(getjGitRepository());
        var testRepositoryWithSpy = getManagedRepository(spyJGitRepository, mockVersioning);
        testRepositoryWithSpy.close();
        testRepositoryWithSpy.close();
        verify(spyJGitRepository, times(1)).close();
    }

    @Test
    public void testRename_repositoryExists_shouldRenameRepository() {
        initTestRepository();
        var newName = "newName";
        testRepository.renameRepository(newName);
        Assertions.assertThat(getWorkTree()).doesNotExist();
        Assertions.assertThat(testRepository.getName()).isEqualTo(newName);
        Assertions.assertThat(getWorkTree(newName)).exists();
    }

    @Test
    public void testRename_newNameExists_shouldThrowException() {
        initTestRepository();
        var newName = testName;
        Assertions.assertThatThrownBy(() -> testRepository.renameRepository(newName))
                .isInstanceOf(RepositoryAlreadyExistsException.class);
    }

    @Test
    public void testRename_repositoryNotExists_shouldNotThrowException() {
        Assertions.assertThatCode(() -> testRepository.renameRepository("newName")).doesNotThrowAnyException();
    }

    @SneakyThrows(IOException.class)
    private Repository getjGitRepository() {
        return new FileRepositoryBuilder()
                .setGitDir(getWorkTree().resolve(".git").toFile())
                .build();
    }

    private ManagedRepository getManagedRepository(Repository repository, ManagedRepositoryVersioning versioning) {
        return new ManagedRepository(repository, testName, StandardCharsets.UTF_8, versioning, repository.getWorkTree().toPath());
    }

    private ManagedRepositoryFile createManagedRepositoryFile(Path path, String content) {
        return new ManagedRepositoryFile(path, content);
    }

    private Path getWorkTree() {
        return tempDir.resolve(testName);
    }

    private Path getWorkTree(String name) {
        return tempDir.resolve(name);
    }

    private void initTestRepository() {
        initRepository(jGitRepository);
    }

    @SneakyThrows(GitAPIException.class)
    private void initRepository(Repository repository) {
        var git = Git.init().setDirectory(repository.getWorkTree()).call();
        git.close();
    }

    private String commitTestRepository(String message) {
        return commitRepository(jGitRepository, message);
    }

    @SneakyThrows(GitAPIException.class)
    private String commitRepository(Repository repository, String message) {
        var git = Git.wrap(repository);
        git.add().addFilepattern(".").call();
        var commit = git.commit().setMessage(message).call();
        git.close();
        return commit.getName();
    }

    private boolean repositoryExists() {
        return jGitRepository.getObjectDatabase() != null && jGitRepository.getObjectDatabase().exists();
    }
}