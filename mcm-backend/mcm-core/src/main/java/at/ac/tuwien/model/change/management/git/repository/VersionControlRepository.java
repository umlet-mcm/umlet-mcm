package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;

public interface VersionControlRepository {

    /**
     * Gets the current version of the repository corresponding to the commit hash of its HEAD.
     * @param repositoryName the name of the repository
     * @return the current version of the repository as a String of its commit hash
     * @throws RepositoryDoesNotExistException if the repository with the given name does not exist
     */
    String getCurrentVersion(String repositoryName) throws RepositoryDoesNotExistException;
}
