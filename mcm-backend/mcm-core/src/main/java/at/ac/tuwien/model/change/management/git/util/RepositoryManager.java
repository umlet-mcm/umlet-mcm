package at.ac.tuwien.model.change.management.git.util;

import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationReadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;


@Component
@Slf4j
@RequiredArgsConstructor
public class RepositoryManager {

    private final GitProperties gitProperties;
    private static final String GIT_DIRECTORY = ".git";

    @FunctionalInterface
    public interface GitCallback<T> {
        T withGit(Git repository);
    }

    @FunctionalInterface
    public interface GitVoidCallback {
        void withGit(Git repository);
    }

    @FunctionalInterface
    public interface RepositoryCallback<T> {
        T withRepository(Repository repository);
    }

    @FunctionalInterface
    public interface RepositoryVoidCallback {
        void withRepository(Repository repository);
    }

    public <T> T withRepository(String repositoryName, boolean mustExist, RepositoryCallback<T> callback) {
        var builder = new FileRepositoryBuilder();
        builder.setGitDir(gitProperties.getRepositoryPath().resolve(repositoryName).resolve(GIT_DIRECTORY).toFile());
        builder.setMustExist(mustExist);
        try (Repository repository = builder.build()) {
            return callback.withRepository(repository);
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to access repository for configuration '" + repositoryName + "'", e);
        }
    }

    public void withRepository(String repositoryName, boolean mustExist, RepositoryVoidCallback callback) {
        withRepository(repositoryName, mustExist, repository -> {
            callback.withRepository(repository);
            return null;
        });
    }

    public <T> T withGit(String repositoryName, GitCallback<T> callback) {
        try (Git git = Git.open(gitProperties.getRepositoryPath().resolve(repositoryName).toFile())) {
            return callback.withGit(git);
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to open repository for configuration '" + repositoryName + "'", e);
        }
    }

    public void withGit(String repositoryName, GitVoidCallback callback) {
        withGit(repositoryName, git -> {
            callback.withGit(git);
            return null;
        });
    }

    public Path gitRepositoriesPath() {
        return gitProperties.getRepositoryPath();
    }
}
