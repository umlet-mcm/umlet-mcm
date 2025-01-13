package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Configuration;

public interface ConfigurationUpdater {
    /**
     * Updates a Configuration with the values of a Configuration from DB
     *
     * @param configuration         the Configuration to update from
     * @param configurationToUpdate the Configuration to update
     */
    void updateConfiguration(Configuration configuration, Configuration configurationToUpdate);
}
