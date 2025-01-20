package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.versioning.ModelDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.NodeDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.RelationDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import lombok.NonNull;

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
    void createConfiguration(@NonNull String name) throws RepositoryAlreadyExistsException;

    /**
     * Updates the name of the configuration with the given current name to the new name.
     *
     * @param currentName current name of the configuration
     * @param newName     new name of the configuration
     * @throws RepositoryAlreadyExistsException if a configuration with the given name already exists
     */
    void renameConfiguration(@NonNull String currentName, @NonNull String newName) throws RepositoryAlreadyExistsException;

    /**
     * Finds the most recent version of the configuration with the given name and reads it from its Git repository.
     * Reads from the files tracked by the most recent commit in the repository - not the working directory.
     *
     * @param name the name of the configuration
     * @return the configuration if it was found - an empty {@link Optional} if no configuration with the given name exists
     * or the existing configuration has no versions yet
     */
    Optional<Configuration> findCurrentVersionOfConfigurationByName(@NonNull String name);

    /**
     * Finds a specified version of the configuration with the given name and reads it from its Git repository.
     * Reads from the files tracked by the commit with the given version in the repository - not the working directory.
     *
     * @param name    the name of the configuration
     * @param version the version of the configuration to retrieve
     * @return the configuration if it was found - an empty {@link Optional} if no configuration with the given name exists
     * or the specified version cannot be resolved for the given configuration
     */
    Optional<Configuration> findSpecifiedVersionOfConfigurationByName(@NonNull String name, @NonNull String version);

    /**
     * Finds all configurations and reads their most recent versions from their Git repositories.
     *
     * @return a list of all configurations
     */
    List<Configuration> findAllConfigurations();

    /**
     * Writes the models, nodes and relations of the given configuration to its Git repository as XML files.
     * The custom commit name is used if provided in the ConfigurationVersion, generic commit name is used in any case.
     * Note that for a new configuration, the Git repository must be created first using {@link #createConfiguration(String)}.
     *
     * @param configuration the configuration to save
     * @return the saved configuration
     * @throws RepositoryDoesNotExistException if the Git repository of the configuration has not been created yet
     */
    Configuration saveConfiguration(@NonNull Configuration configuration) throws RepositoryDoesNotExistException;

    /**
     * Deletes the Git repository of the configuration with the given name and all its contents.
     *
     * @param name the name of the configuration
     */
    void deleteConfiguration(@NonNull String name);

    /**
     * Compares two versions of a configuration in the given repository.
     * Produces a unified diff output along with some additional information for each file that a diff was computed for
     *
     * @param name             the name of the configuration
     * @param oldVersion       the old version to compare with
     * @param newVersion       the new version to compare with
     * @param includeUnchanged whether to include unchanged models, nodes or relations in the comparison results
     *                         if set to true, these will be included as "UNCHANGED" diff entries with the content simply
     *                         set to the XML representation of the model, node or relation (i.e., not including any Git headers or hunks)
     * @return a {@link ConfigurationContents} object containing the differences between the two versions of the configuration
     * @throws RepositoryDoesNotExistException if the Git repository of the configuration does not exist
     */
    ConfigurationContents<ModelDiff, NodeDiff, RelationDiff> compareConfigurationVersions(
            @NonNull String name,
            @NonNull String oldVersion,
            @NonNull String newVersion,
            boolean includeUnchanged
    ) throws RepositoryDoesNotExistException;
}
