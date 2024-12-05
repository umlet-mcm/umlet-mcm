package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class RepositoryManagerImpl implements RepositoryManager{

    private final GitProperties gitProperties;

    @Override
    public Repository accessRepository(@NonNull String repositoryName) {
        return getRepositoryByName(repositoryName);
    }

    @Override
    public List<Repository> listRepositories() {
        try (var fileStream = Files.list(repositoriesPath())) {
            log.debug("Listing repositories in '{}'.", repositoriesPath());
            return fileStream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(this::getRepositoryByName)
                    .filter(RepositoryUtils::repositoryExists)
                    .toList();
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not list directories in '" + repositoriesPath() + "'");
        }
    }

    private Repository getRepositoryByName(String repositoryName) {
        try {
            log.debug("Accessing repository '{}'.", repositoryName);
            var repositoryDir = repositoriesPath().resolve(repositoryName);
            return RepositoryUtils.getRepositoryAtPath(repositoryDir);
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not access repository '" + repositoryName + "'");
        }
    }

    private Path repositoriesPath() {
        return Optional.ofNullable(gitProperties.getRepositories())
                .orElseThrow(() -> new RepositoryAccessException("Repository path is not configured."));
    }
}
