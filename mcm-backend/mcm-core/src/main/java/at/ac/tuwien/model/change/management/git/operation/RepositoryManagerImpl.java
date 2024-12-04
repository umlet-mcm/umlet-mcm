package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@GitComponent
@Slf4j
public class RepositoryManagerImpl implements RepositoryManager{

    private final Path repositoryPath;

    public RepositoryManagerImpl(GitProperties gitProperties) {
        this.repositoryPath = Optional.ofNullable(gitProperties.getRepositoryPath())
                .orElseThrow(() -> new IllegalArgumentException("Repository path is not configured"));
    }

    @Override
    public Repository accessRepository(@NonNull String repositoryName) {
        return getRepositoryByName(repositoryName);
    }

    @Override
    public List<Repository> listRepositories() {
        try (var fileStream = Files.list(repositoryPath)) {
            log.debug("Listing repositories in '{}'.", repositoryPath);
            return fileStream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(this::getRepositoryByName)
                    .filter(RepositoryUtils::repositoryExists)
                    .toList();
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not list directories in '" + repositoryPath + "'");
        }
    }

    private Repository getRepositoryByName(String repositoryName) {
        try {
            log.debug("Accessing repository '{}'.", repositoryName);
            var repositoryDir = repositoryPath.resolve(repositoryName);
            return RepositoryUtils.getRepositoryAtPath(repositoryDir);
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not access repository '" + repositoryName + "'");
        }
    }
}
