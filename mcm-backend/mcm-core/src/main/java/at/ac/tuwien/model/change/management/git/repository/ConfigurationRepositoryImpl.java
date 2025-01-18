package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.versioning.ModelDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.NodeDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.RelationDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.git.infrastructure.ManagedRepository;
import at.ac.tuwien.model.change.management.git.operation.ConfigurationRepositoryActions;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.*;
import at.ac.tuwien.model.change.management.git.infrastructure.RepositoryManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class ConfigurationRepositoryImpl implements ConfigurationRepository {

    private final ConfigurationRepositoryActions repositoryActions;
    private final RepositoryManager repositoryManager;

    @Override
    public void createConfiguration(@NonNull String name) throws RepositoryAlreadyExistsException {
        log.debug("Creating repository for configuration '{}'.", name);
        repositoryManager.consumeRepository(name, repository -> {
            if (repository.exists()) {
                throw new RepositoryAlreadyExistsException("Repository for configuration '" + name + "' already exists.");
            }
            repository.versioning().init();
            log.info("Created repository for configuration '{}'.", name);
        });
    }

    @Override
    public Optional<Configuration> findCurrentVersionOfConfigurationByName(@NonNull String name) {
        log.debug("Finding current version of configuration '{}'.", name);
        return withExistingConfiguration(name, repository -> {
            var optionalConfiguration = repositoryActions.readCurrentConfigurationVersion(repository);
            optionalConfiguration.ifPresentOrElse(
                    configuration -> log.info("Found current version '{}' of configuration '{}'.", configuration.getVersion(), name),
                    () -> log.warn("Current version of configuration '{}' could not be found.", name)
            );
            return optionalConfiguration;
        });
    }

    @Override
    public Optional<Configuration> findSpecifiedVersionOfConfigurationByName(@NonNull String name, @NonNull String version) {
        log.debug("Finding version '{}' of configuration '{}'.", version, name);
        return withExistingConfiguration(name, repository -> {
            var optionalConfiguration = repositoryActions.readConfigurationVersion(repository, version);
            optionalConfiguration.ifPresentOrElse(
                    configuration -> log.info("Found version '{}' of configuration '{}'.", configuration.getVersion(), name),
                    () -> log.warn("Version '{}' of configuration '{}' could not be found.", version, name)
            );
            return optionalConfiguration;
        });
    }

    @Override
    public List<Configuration> findAllConfigurations() {
        log.debug("Searching all repositories for configurations.");
        return repositoryManager.withAllRepositories(repositories -> {
            var configurations = repositories.stream()
                    .map(repositoryActions::readCurrentConfigurationVersion)
                    .flatMap(Optional::stream)
                    .toList();
            log.debug("Found {} configurations in repositories.", configurations.size());
            return configurations;
        });
    }

    @Override
    public Configuration saveConfiguration(@NonNull Configuration configuration) throws RepositoryDoesNotExistException {
        log.debug("Saving configuration '{}'.", configuration.getName());
        return repositoryManager.withRepository(configuration.getName(), repository -> {
            if (!repository.exists()) {
                throw new RepositoryDoesNotExistException("Repository for configuration '" + configuration.getName() + "' does not exist.");
            }
            repositoryActions.clearConfigurationRepository(repository);
            repositoryActions.writeConfigurationToWorkingDirectory(repository, configuration);
            var filesCount = repository.versioning().stageAll();
            var version = repository.versioning().commit(
                    "Updated configuration '" + configuration.getName() + "' with " + filesCount + " entries",
                    true
            );
            log.info("Created new version '{}' of configuration: {}.", version, configuration.getName());
            return repositoryActions.readCurrentConfigurationVersion(repository)
                    .orElseThrow(() -> new RepositoryVersioningException("Failed to read new version '" +
                            version + "' of configuration '" + configuration.getName() + "' after saving it to repository"));
        });
    }

    @Override
    public void deleteConfiguration(@NonNull String name) {
        log.debug("Deleting repository for configuration: {}.", name);
        repositoryManager.consumeRepository(name, ManagedRepository::deleteRepository);
        log.info("Deleted repository for configuration: {}", name);
    }

    @Override
    public ConfigurationContents<ModelDiff, NodeDiff, RelationDiff> compareConfigurationVersions(
            @NonNull String name,
            @NonNull String oldVersion,
            @NonNull String newVersion,
            boolean includeUnchanged
    ) throws RepositoryDoesNotExistException {
        log.debug("Comparing versions '{}' and '{}' of configuration '{}'.", oldVersion, newVersion, name);
        return repositoryManager.withRepository(name, repository -> {
            if (!repository.exists()) {
                throw new RepositoryDoesNotExistException("Repository for configuration '" + name + "' does not exist.");
            }

            var configurationComparison = repositoryActions.compareConfigurationVersions(repository, oldVersion, newVersion, includeUnchanged);
            log.info("Created comparison of versions '{}' and '{}' for configuration '{}'.", oldVersion, newVersion, name);
            return configurationComparison;
        });
    }

    private Optional<Configuration> withExistingConfiguration(String name, Function<ManagedRepository, Optional<Configuration>> action) {
        return repositoryManager.withRepository(name, repository -> {
            if (!repository.exists()) {
                log.warn("Repository for configuration '{}' does not exist.", name);
                return Optional.empty();
            }
            return action.apply(repository);
        });
    }
}
