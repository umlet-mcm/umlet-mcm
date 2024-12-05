package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.util.RepositoryContents;
import org.eclipse.jgit.lib.Repository;

import java.nio.file.Path;

public interface ConfigurationIOManager {

    RepositoryContents<Path> writeConfigurationToRepository(Repository repository, Configuration configuration);

    Configuration readConfigurationFromRepository(Repository repository, String version);

    void clearConfigurationRepository(Repository repository);

}
