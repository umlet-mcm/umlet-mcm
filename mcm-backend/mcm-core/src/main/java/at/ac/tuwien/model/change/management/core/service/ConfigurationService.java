package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.versioning.BaseAttributesDiff;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Service for managing configurations
 */
public interface ConfigurationService {


    /**
     * Create a new configuration
     * Version must NOT be set for the configuration, since it has not been versioned yet
     * IDs can be set for models, nodes or relations, but are not required - there can be no duplicate IDs
     *
     * @param configuration the configuration to create
     * @return the created configuration
     */
    Configuration createConfiguration(@NonNull Configuration configuration, boolean loadIntoGraphDB);


    /**
     * Update an existing configuration
     * Version must be set for the configuration, since it has already been versioned
     * IDs must be set for models, nodes or relations that are updated, but are not required for new models, nodes or relations
     *
     * @param configuration the configuration to update
     * @return the updated configuration
     */
    Configuration updateConfiguration(@NonNull Configuration configuration, boolean loadIntoGraphDB);

    /**
     * Delete a configuration with the given name
     * @param name the name of the configuration to delete
     */
    void deleteConfiguration(@NonNull String name);

    /**
     * Get the most recent version a configuration by its name
     * @param name the name of the configuration
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the most recent version of the configuration with the given name
     */
    Configuration getConfigurationByName(@NonNull String name, boolean loadIntoGraphDB);

    /**
     * Get a specific version of a configuration by its name and version
     * @param name the name of the configuration
     * @param version the version of the configuration
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the configuration with the given name and version
     */
    Configuration getConfigurationVersion(@NonNull String name, @NonNull String version, boolean loadIntoGraphDB);

    /**
     * Get the most recent versions of all configurations
     * @return a list of the most recent versions of all configurations
     */
    List<Configuration> getAllConfigurations();

    /**
     * List all versions of a configuration by its name
     * @param name the name of the configuration
     * @return a list of all versions of the configuration with the given name
     */
    List<String> listConfigurationVersions(@NonNull String name);

    /**
     * Compare two versions of a configuration by their identifiers
     * @param name the name of the configuration
     * @param oldVersion the old version of the configuration
     * @param newVersion the new version of the configuration
     * @param includeUnchanged whether to include unchanged models, nodes or relations in the comparison results
     *                         if set to true, these will be included as "UNCHANGED" diff entries with the content simply
     *                         set to the XML representation of the model, node or relation (i.e., not including any Git headers or hunks)
     * @return a list of differences between the two versions of the configuration
     */
    List<BaseAttributesDiff> compareConfigurationVersions(
            @NonNull String name,
            @NonNull String oldVersion,
            @NonNull String newVersion,
            boolean includeUnchanged
    );

    /**
     * Checkout a specific version of a configuration by its name
     * @param name the name of the configuration
     * @param version the version to `git checkout`
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the configuration with the given name and version
     * NOTE that this is - as currently implemented - is actual `git checkout`
     * so it does not move the main branch, just the HEAD pointer
     */
    Configuration checkoutConfigurationVersion(@NonNull String name, @NonNull String version, boolean loadIntoGraphDB);

    /**
     * Reset a configuration to a specific version by its name
     * @param name the name of the configuration to reset
     * @param version the version to reset to
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the configuration with the given name and version that it has been reset to
     * NOTE that this is - as currently implemented - is actual `git reset`
     * unlike checkout it will move the main branch to the specified version
     */
    Configuration resetConfigurationVersion(@NonNull String name, @Nullable String version, boolean loadIntoGraphDB);

    /**
     * Get the current version of the configuration by its name
     * automatically load it into the graph database
     * @param configuration the configuration to get the current version of
     * @return the current version of the configuration
     */
    default Configuration createConfiguration(@NonNull Configuration configuration) {
        return createConfiguration(configuration, true);
    }

    /**
     * Update an existing configuration
     * automatically load it into the graph database
     * @param configuration the configuration to update
     * @return the updated configuration
     */
    default Configuration updateConfiguration(@NonNull Configuration configuration) {
        return updateConfiguration(configuration, true);
    }

    /**
     * Get the most recent version of a configuration by its name
     * do NOT load it into the graph database
     * @param name the name of the configuration
     * @return the most recent version of the configuration with the given name
     */
    default Configuration getConfigurationByName(@NonNull String name) {
        return getConfigurationByName(name, false);
    }

    /**
     * Get a specific version of a configuration by its name and version
     * do NOT load it into the graph database
     * @param name the name of the configuration
     * @param version the version of the configuration
     * @return the configuration with the given name and version
     */
    default Configuration getConfigurationVersion(@NonNull String name, @NonNull String version) {
        return getConfigurationVersion(name, version, false);
    }


    /**
     * Checkout a specific version of a configuration by its name
     * do NOT load it into the graph database
     * @param name the name of the configuration
     * @param version the version to `git checkout`
     * @return the configuration with the given name and version
     */
    default Configuration checkoutConfigurationVersion(@NonNull String name, @NonNull String version) {
        return checkoutConfigurationVersion(name, version, false);
    }

    /**
     * Reset a configuration to a specific version by its name
     * do NOT load it into the graph database
     * @param name the name of the configuration to reset
     * @param version the version to reset to
     * @return the configuration with the given name and version that it has been reset to
     */
    default Configuration resetConfigurationVersion(@NonNull String name, @Nullable String version) {
        return resetConfigurationVersion(name, version, false);
    }

}
