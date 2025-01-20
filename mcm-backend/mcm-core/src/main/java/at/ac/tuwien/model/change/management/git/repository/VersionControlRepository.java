package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface VersionControlRepository {

    /**
     * Gets the current version of the repository corresponding to the commit hash of its HEAD.
     * @param repositoryName the name of the repository
     * @return Optional containing the commit hash of the HEAD of the repository, or empty if the repository does not exist or has no commits
     */
    Optional<String> getCurrentVersion(@NonNull String repositoryName);

    /**
     * Lists all versions of the repository.
     * @param repositoryName the name of the repository
     * @return a list of commit hashes representing the versions of the repository
     */
    List<String> listVersions(@NonNull String repositoryName);

    /**
     * Checks out the version of the repository corresponding to the given commit hash.
     * @param repositoryName the name of the repository
     * @param version the commit hash or other version identifier of the version to check out
     * @throws RepositoryDoesNotExistException if the repository does not exist
     */
    void checkoutVersion(@NonNull String repositoryName, @NonNull String version) throws RepositoryDoesNotExistException;

    /**
     * Resets the repository to the version corresponding to the given commit hash.
     * @param repositoryName the name of the repository
     * @param version the commit hash or other version identifier of the version to reset to
     * @throws RepositoryDoesNotExistException if the repository does not exist
     */
    void resetToVersion(@NonNull String repositoryName, @NonNull String version) throws RepositoryDoesNotExistException;

}
