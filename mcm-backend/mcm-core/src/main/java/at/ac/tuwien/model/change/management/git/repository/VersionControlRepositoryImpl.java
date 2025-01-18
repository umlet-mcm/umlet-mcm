package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.infrastructure.RepositoryManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class VersionControlRepositoryImpl implements VersionControlRepository {

    private final RepositoryManager repositoryManager;

    @Override
    public Optional<String> getCurrentVersion(@NonNull String repositoryName) {
        log.debug("Resolving HEAD of repository '{}'.", repositoryName);
        return repositoryManager.withRepository(repositoryName, repository -> {
            if (! repository.exists()) {
                log.warn("Repository '{}' does not exist.", repositoryName);
                return Optional.empty();
            }
            return repository.versioning().getCurrentVersionId()
                    .map(currentVersion -> {
                        log.info("Resolved HEAD of repository '{}' to '{}'.", repositoryName, currentVersion);
                        return currentVersion;
                    })
                    .or(() -> {
                        log.warn("Repository '{}' has no commits.", repositoryName);
                        return Optional.empty();
                    });
        });
    }

    @Override
    public List<String> listVersions(@NonNull String repositoryName) {
        log.debug("Listing versions of repository '{}'.", repositoryName);
        return repositoryManager.withRepository(repositoryName, repository -> {
            var versions = repository.versioning().listVersions();
            log.info("Listed {} versions of repository '{}'", versions.size(), repositoryName);
            return versions;
        });
    }

    @Override
    public void checkoutVersion(@NonNull String repositoryName, @NonNull String version) throws RepositoryDoesNotExistException {
        log.debug("Checking out version '{}' of repository '{}'.", version, repositoryName);
        repositoryManager.consumeRepository(repositoryName, repository -> {
            if (! repository.exists()) {
                throw new RepositoryDoesNotExistException("Repository '" + repositoryName + "' does not exist.");
            }
            repository.versioning().checkout(version);
            log.info("Checked out version '{}' of repository '{}'.", version, repositoryName);
        });
    }

    @Override
    public void resetToVersion(@NonNull String repositoryName, @NonNull String version) {
        log.debug("Resetting repository '{}' to version '{}'.", repositoryName, version);
        repositoryManager.consumeRepository(repositoryName, repository -> {
            if (! repository.exists()) {
                throw new RepositoryDoesNotExistException("Repository '" + repositoryName + "' does not exist.");
            }
            repository.versioning().reset(version);
            log.info("Reset repository '{}' to version '{}'.", repositoryName, version);
        });
    }
}
