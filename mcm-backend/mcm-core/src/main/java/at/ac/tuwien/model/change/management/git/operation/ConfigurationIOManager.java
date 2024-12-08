package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.util.RepositoryContents;
import org.eclipse.jgit.lib.Repository;

import java.nio.file.Path;

/**
 * Manages the input/output operations of a configuration to/from its Git repository.
 */
public interface ConfigurationIOManager {

    /**
     * Writes the given configuration to its Git repository.
     * @param repository the Git repository of the configuration
     * @param configuration the configuration to write
     * @return the paths of the written files in the repository wrapped in a {@link RepositoryContents} object
     */
    RepositoryContents<Path> writeConfigurationToRepository(Repository repository, Configuration configuration);

    /**
     * Reads the configuration with the given version from its Git repository.
     * @param repository the Git repository of the configuration
     * @param version the version of the configuration to read - corresponds to a Git commit hash
     * @return the configuration read from the repository
     */
    Configuration readConfigurationFromRepository(Repository repository, String version);

    /**
     * Clears the contents of the Git repository working directory of the given configuration.
     * Note that this does not delete the Git repository itself, nor the configuration versions it contains.
     * Can be called when saving a configuration update - First, clear the working directory, then write the updated configuration.
     *
     * @param repository the Git repository of the configuration
     */
    void clearConfigurationRepository(Repository repository);

}
