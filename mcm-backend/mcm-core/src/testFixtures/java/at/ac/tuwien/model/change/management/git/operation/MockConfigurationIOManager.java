package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.util.RepositoryContents;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;

import java.nio.file.Path;
import java.util.HashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jgit.lib.Repository;

public class MockConfigurationIOManager implements ConfigurationIOManager {

    private final HashMap<String, Configuration> configurations = new HashMap<>();

    @Override
    public RepositoryContents<Path> writeConfigurationToRepository(Repository repository, Configuration configuration) {
        configuration.setVersion(generateCommitHash());
        configurations.put(configuration.getName(), configuration);
        return new RepositoryContents<>();
    }

    @Override
    public Configuration readConfigurationFromRepository(Repository repository, String version) {
        return configurations.get(RepositoryUtils.getRepositoryName(repository));
    }

    @Override
    public void clearConfigurationRepository(Repository repository) {
        configurations.get(RepositoryUtils.getRepositoryName(repository)).setModels(null);
    }

    private String generateCommitHash() {
        return RandomStringUtils.randomAlphanumeric(40).toLowerCase();
    }
}
