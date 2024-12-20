package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;

import java.util.List;
import java.util.Optional;

/**
 * Data access object for configurations stored in Git repositories.
 */
public interface ConfigurationRepository {

    /**
     * Creates a new Git repository for the configuration with the given name.
     *
     * @param name the name of the configuration
     * @throws RepositoryAlreadyExistsException if a configuration with the given name already exists
     */
    void createConfiguration(String name) throws RepositoryAlreadyExistsException;

    /**
     * Finds a configuration by its name and reads it from its Git repository.
     *
     * @param name the name of the configuration
     * @return the configuration with the given name wrapped in an {@link Optional}
     * or an empty {@link Optional} if no configuration with the given name exists
     */
    Optional<Configuration> findConfigurationByName(String name);

    /**
     * Finds all configurations and reads them from their Git repositories.
     *
     * @return a list of all configurations
     */
    List<Configuration> findAllConfigurations();

    /**
     * Writes the models, nodes and relations of the given configuration to its Git repository as XML files.
     * Note that for a new configuration, the Git repository must be created first using {@link #createConfiguration(String)}.
     *
     * @param configuration the configuration to save
     * @return the saved configuration
     * @throws RepositoryDoesNotExistException if the Git repository of the configuration has not been created yet
     */
    Configuration saveConfiguration(Configuration configuration) throws RepositoryDoesNotExistException;

    /**
     * Deletes the Git repository of the configuration with the given name and all its contents.
     * @param name the name of the configuration
     */
    void deleteConfiguration(String name);
}
