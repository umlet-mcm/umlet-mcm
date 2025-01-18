package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.versioning.ModelDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.NodeDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.RelationDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.git.infrastructure.ManagedRepository;
import lombok.NonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ConfigurationRepositoryActions {

    /**
     * Writes the DSL representation of the given configuration to the working directory of the given repository.
     * @param repository the repository to write the configuration to
     * @param configuration the configuration to write
     * @return a list of paths to the written files
     */
    List<Path> writeConfigurationToWorkingDirectory(
            @NonNull ManagedRepository repository,
            @NonNull Configuration configuration
    );

    /**
     * Reads the current configuration version from the given repository
     * by parsing the DSL files to their corresponding configuration object.
     * Note that this does not read from the working directory but from the files tracked in the HEAD git commit.
     * @param repository the repository to read the configuration from
     * @return the current configuration version or an empty optional if there is no most recent version of the configuration
     * usually, if there is no most recent version, there are no versions at all
     * so this method should return an empty optional only if there has never been a commit in the repository
     */
    Optional<Configuration> readCurrentConfigurationVersion(@NonNull ManagedRepository repository);

    /**
     * Reads the configuration version with the given version from the given repository
     * @param repository the repository to read the configuration from
     * @param version the version of the configuration to read
     * @return the configuration version with the given version or an empty optional if there is no such version
     */
    Optional<Configuration> readConfigurationVersion(@NonNull ManagedRepository repository, @NonNull String version);

    /**
     * Clears the configuration working directory of the given repository.
     * More specifically, it deletes the directories that store the models, nodes and relations of the configuration
     * in their DSL format from the working tree.
     * Callers will still be able to read from repository commits, e.g., using {@link #readCurrentConfigurationVersion(ManagedRepository)}
     * @param repository the repository to clear the configuration working directory of
     */
    void clearConfigurationRepository(@NonNull ManagedRepository repository);

    /**
     * Compares two versions of a configuration in the given repository.
     * Produces a unified diff output along with some additional information for each file that a diff was computed for
     *
     * @param repository the repository to compare the configuration versions in
     * @param oldVersion the old version to compare with
     * @param newVersion the new version to compare with
     * @param includeUnchanged whether to include unchanged models, nodes or relations in the comparison results
     *                         if set to true, these will be included as "UNCHANGED" diff entries with the content simply
     *                         set to the XML representation of the model, node or relation (i.e., not including any Git headers or hunks)
     * @return a {@link ConfigurationContents} object containing the differences between the two versions of the configuration
     */
    ConfigurationContents<ModelDiff, NodeDiff, RelationDiff> compareConfigurationVersions(
            @NonNull ManagedRepository repository,
            @NonNull String oldVersion,
            @NonNull String newVersion,
            boolean includeUnchanged
    );
}
