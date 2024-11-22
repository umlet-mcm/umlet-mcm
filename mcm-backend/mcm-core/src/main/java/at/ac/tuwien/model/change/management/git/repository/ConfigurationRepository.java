package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.List;

public interface ConfigurationRepository {
    Configuration create(Configuration configuration);

    Configuration update(Configuration configuration);

    void delete(String name);

    Configuration findConfigurationByName(String name);

    List<Configuration> findAll();

    List<DiffEntry> diff(Configuration configuration, String oldCommitHash, String newCommitHash);
}
