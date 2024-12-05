package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import at.ac.tuwien.model.change.management.git.operation.RepositoryManager;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;

import java.io.IOException;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class VersionControlRepositoryImpl implements VersionControlRepository {

    private final RepositoryManager repositoryManager;

    @Override
    public String getCurrentVersion(String repositoryName) throws RepositoryDoesNotExistException {
        try (var repository = repositoryManager.accessRepository(repositoryName)) {
            log.debug("Resolving HEAD of repository '{}'.", repositoryName);

            if (!RepositoryUtils.repositoryExists(repository)) {
                throw new RepositoryDoesNotExistException("Repository '" + repositoryName + "' does not exist.");
            }

            var version = RepositoryUtils.resolveCommit(repository, Constants.HEAD).map(AnyObjectId::getName)
                    .orElseThrow(() -> new RepositoryReadException("Could not resolve HEAD of repository '" + repositoryName + "'."));

            log.info("Resolved HEAD of repository '{}' to '{}'.", repositoryName, version);
            return version;
        } catch (IOException e) {
            throw new RepositoryReadException("Could not resolve HEAD of repository '" + repositoryName + "'.", e);
        }
    }
}
