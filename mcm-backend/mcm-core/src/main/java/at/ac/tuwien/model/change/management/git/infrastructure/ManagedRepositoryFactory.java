package at.ac.tuwien.model.change.management.git.infrastructure;

import java.util.List;

/**
 * Factory creating managed repositories (wrappers around JGit repositories) and corresponding objects.
 */
public interface ManagedRepositoryFactory {

    /**
     * Creates a new managed repository.
     * NOTE that this method creates the in-memory object, but does not initialize it or write it to disk.
     * For that purpose, use the {@link ManagedRepositoryVersioning#init()} method.
     *
     * @param name the name of the repository to create
     * @return the created managed repository
     */
    ManagedRepository getRepositoryByName(String name);

    /**
     * Get all managed repositories stored at the path defined in
     * Only retrieves repositories that have been initialized and have a working directory.
     * (Implementing this otherwise would be quite difficult without immediately initializing all repositories)
     * {@link at.ac.tuwien.model.change.management.git.config.GitProperties}
     * @return a list of all managed repositories
     */
    List<ManagedRepository> getAllRepositories();
}
