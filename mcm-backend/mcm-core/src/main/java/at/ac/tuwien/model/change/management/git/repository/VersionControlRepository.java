package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;

public interface VersionControlRepository {

    String getCurrentVersion(String repositoryName) throws RepositoryDoesNotExistException;
}
