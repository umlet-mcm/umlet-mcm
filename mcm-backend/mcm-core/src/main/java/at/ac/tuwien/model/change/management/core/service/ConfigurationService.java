package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;

import java.util.List;

public interface ConfigurationService {

    Configuration createConfiguration(Configuration configuration);

    Configuration updateConfiguration(Configuration configuration);

    void deleteConfiguration(Configuration configuration);

    Configuration getConfigurationByName(String name);

    List<Configuration> getAllConfigurations();
}
