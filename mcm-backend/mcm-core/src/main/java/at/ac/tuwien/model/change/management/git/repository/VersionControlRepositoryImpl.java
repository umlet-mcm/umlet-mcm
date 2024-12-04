package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import at.ac.tuwien.model.change.management.git.operation.RepositoryManager;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;

import java.io.IOException;
import java.util.Optional;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class VersionControlRepositoryImpl implements VersionControlRepository {

    private final RepositoryManager repositoryManager;

    @Override
    public Optional<String> getCurrentVersion(String repositoryName) {
        try (var repository = repositoryManager.accessRepository(repositoryName)) {
            return RepositoryUtils.resolveCommit(repository, Constants.HEAD)
                    .map(AnyObjectId::getName);
        } catch (IOException e) {
            throw new RepositoryReadException("Could not resolve HEAD of repository '" + repositoryName + "'.", e);
        }
    }
}
