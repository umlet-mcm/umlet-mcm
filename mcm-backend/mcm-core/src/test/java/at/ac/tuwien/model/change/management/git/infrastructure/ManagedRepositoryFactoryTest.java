package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagedRepositoryFactoryTest {

    @TempDir
    private Path tempDir;

    @Mock
    GitProperties properties;

    @BeforeEach
    public void setup() {
        when(properties.getRepositories()).thenReturn(tempDir);
    }

    @InjectMocks
    ManagedRepositoryFactoryImpl factory;

    @Test
    public void testGetRepositoryByName_repositoryExists_shouldReturnRepository() {
        var name = "test";
        initRepository(name);
        var repository = factory.getRepositoryByName(name);
        Assertions.assertThat(repository).isNotNull();
    }

    @Test
    public void testGetRepositoryByName_repositoryDoesNotExist_shouldReturnRepository() {
        var name = "test";
        var repository = factory.getRepositoryByName(name);
        Assertions.assertThat(repository).isNotNull();
    }

    @Test
    public void testGetRepositoryByName_repositoryOutsideRepositoriesDir_shouldThrowRepositoryAccessException() {
        var name = "../test";
        Assertions.assertThatThrownBy(() -> factory.getRepositoryByName(name))
                .isInstanceOf(RepositoryAccessException.class);
    }

    @Test
    public void testGetRepositoryByName_repositoryDirDoesNotExist_shouldThrowRepositoryAccessException() {
        var name = "test";
        when(properties.getRepositories()).thenReturn(tempDir.resolve("nonExistentDirectory"));
        Assertions.assertThatThrownBy(() -> factory.getRepositoryByName(name))
                .isInstanceOf(RepositoryAccessException.class);
    }

    @Test
    public void testGetAllRepositories_noRepositories_shouldReturnEmptyList() {
        var repositories = factory.getAllRepositories();
        Assertions.assertThat(repositories).isEmpty();
    }

    @Test
    public void testGetAllRepositories_oneRepository_shouldReturnRepository() {
        var name = "test";
        initRepository(name);
        var repositories = factory.getAllRepositories();
        Assertions.assertThat(repositories)
                .hasSize(1)
                .extracting(ManagedRepository::getName)
                .containsExactly(name);
    }

    @Test
    public void testGetAllRepositories_threeRepositories_shouldReturnRepositories() {
        var repositoryNames = List.of("test1", "test2", "test3");
        repositoryNames.forEach(this::initRepository);
        var repositories = factory.getAllRepositories();
        Assertions.assertThat(repositories)
                .hasSize(3)
                .extracting(ManagedRepository::getName)
                .containsExactlyInAnyOrderElementsOf(repositoryNames);
    }

    @Test
    public void testGetAllRepositories_repositoryDirDoesNotExist_shouldThrowRepositoryAccessException() {
        when(properties.getRepositories()).thenReturn(tempDir.resolve("nonExistentDirectory"));
        Assertions.assertThatThrownBy(factory::getAllRepositories)
                .isInstanceOf(RepositoryAccessException.class);
    }

    @Test
    @SuppressWarnings("resource")
    public void testGetAllRepositories_filesListThrowsIOException_shouldThrowRepositoryAccessException() {
        try(var filesMock = mockStatic(Files.class)) {
           filesMock.when(() -> Files.list(any())).thenThrow(new IOException());
           Assertions.assertThatThrownBy(factory::getAllRepositories)
                   .isInstanceOf(RepositoryAccessException.class);
        }
    }

    @SneakyThrows(GitAPIException.class)
    private void initRepository(String name) {
        var git = Git.init().setDirectory(tempDir.resolve(name).toFile()).call();
        git.close();
    }
}
