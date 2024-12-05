package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;

import java.util.List;
import java.util.Optional;

public interface ConfigurationRepository {

    void createConfiguration(String name) throws RepositoryAlreadyExistsException;

    Optional<Configuration> findConfigurationByName(String name);

    List<Configuration> findAllConfigurations();

    Configuration saveConfiguration(Configuration configuration) throws RepositoryDoesNotExistException;

    void deleteConfiguration(String name);
}
