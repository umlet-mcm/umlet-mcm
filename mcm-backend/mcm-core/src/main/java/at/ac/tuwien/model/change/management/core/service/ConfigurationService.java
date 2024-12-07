package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;

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
    Configuration createConfiguration(Configuration configuration);

    /**
     * Update an existing configuration
     * Version must be set for the configuration, since it has already been versioned
     * IDs must be set for models, nodes or relations that are updated, but are not required for new models, nodes or relations
     *
     * @param configuration the configuration to update
     * @return the updated configuration
     */
    Configuration updateConfiguration(Configuration configuration);

    /**
     * Delete a configuration with the given name
     * @param name the name of the configuration to delete
     */
    void deleteConfiguration(String name);

    /**
     * Get a configuration by its name
     *
     * @param name the name of the configuration
     * @return the configuration with the given name
     */
    Configuration getConfigurationByName(String name);

    /**
     * Get all configurations
     * @return a list of all configurations
     */
    List<Configuration> getAllConfigurations();
}
