package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;

public interface ConfigurationRepository {
    Configuration create(Configuration configuration);
}
