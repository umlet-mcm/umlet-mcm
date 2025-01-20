package at.ac.tuwien.model.change.management.testutil;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.versioning.ModelDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.NodeDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.RelationDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class MockConfigurationRepository implements ConfigurationRepository {

    private final HashMap<String, LinkedHashMap<String, Configuration>> configurations = new HashMap<>();

    @Override
    public void createConfiguration(@NonNull String name) throws RepositoryAlreadyExistsException {
        if (configurations.containsKey(name)) {
            throw new RepositoryAlreadyExistsException("Configuration with name " + name + " already exists");
        }
        var configuration = new Configuration();
        configuration.setName(name);
        var version = configuration.getVersion() == null ? generateSHA1Version() : configuration.getVersion();
        configuration.setVersion(version);
        configurations.computeIfAbsent(name, k -> new LinkedHashMap<>()).put(version, configuration);
    }

    @Override
    public Optional<Configuration> findCurrentVersionOfConfigurationByName(@NonNull String name) {
        var configurationVersions = configurations.get(name);
        return configurationVersions == null || configurationVersions.isEmpty()
                ? Optional.empty()
                : Optional.of(getFirstValue(configurationVersions));
    }

    @Override
    public Optional<Configuration> findSpecifiedVersionOfConfigurationByName(@NonNull String name, @NonNull String version) {
        var configurationVersions = configurations.get(name);
        return configurationVersions == null || configurationVersions.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(configurationVersions.get(version));
    }

    @Override
    public List<Configuration> findAllConfigurations() {
        return configurations.values().stream()
                .map(this::getFirstValue)
                .toList();
    }

    @Override
    public Configuration saveConfiguration(@NonNull Configuration configuration) throws RepositoryDoesNotExistException {
        var configurationVersions = configurations.get(configuration.getName());
        if (configurationVersions == null) {
            throw new RepositoryDoesNotExistException("Configuration with name " + configuration.getName() + " does not exist");
        }
        configuration.setVersion(generateSHA1Version());
        configurationVersions.putFirst(configuration.getVersion(), configuration);
        return configuration;
    }

    @Override
    public void deleteConfiguration(@NonNull String name) {
        configurations.remove(name);
    }

    @Override
    public ConfigurationContents<ModelDiff, NodeDiff, RelationDiff> compareConfigurationVersions(@NonNull String name, @NonNull String oldVersion, @NonNull String newVersion, boolean includeUnchanged) throws RepositoryDoesNotExistException {
        throw new UnsupportedOperationException("Not implemented in this mock");
    }

    private <T> T getFirstValue(LinkedHashMap<?, T> map) {
        return map.entrySet().iterator().next().getValue();
    }

    private String generateSHA1Version() {
        return RandomStringUtils.randomAlphanumeric(40).toLowerCase();
    }
}


