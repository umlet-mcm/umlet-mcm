package at.ac.tuwien.model.change.management.testutil;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MockConfigurationRepository implements ConfigurationRepository {

    private final HashMap<String, Configuration> configurations = new HashMap<>();

    @Override
    public void createConfiguration(String name) throws RepositoryAlreadyExistsException {
        if (configurations.containsKey(name)) {
            throw new RepositoryAlreadyExistsException("Configuration with name " + name + " already exists");
        }
        var configuration = new Configuration();
        configuration.setName(name);
        configurations.put(name, configuration);
    }

    @Override
    public Optional<Configuration> findConfigurationByName(String name) {
        return Optional.ofNullable(configurations.get(name));
    }

    @Override
    public List<Configuration> findAllConfigurations() {
        return List.copyOf(configurations.values());
    }

    @Override
    public Configuration saveConfiguration(Configuration configuration) throws RepositoryDoesNotExistException {
        if (!configurations.containsKey(configuration.getName())) {
            throw new RepositoryDoesNotExistException("Configuration with name " + configuration.getName() + " does not exist");
        }
        configuration.setVersion(RandomStringUtils.randomAlphanumeric(40).toLowerCase());
        configurations.put(configuration.getName(), configuration);
        return configuration;
    }

    @Override
    public void deleteConfiguration(String name) {
        configurations.remove(name);
    }
}

