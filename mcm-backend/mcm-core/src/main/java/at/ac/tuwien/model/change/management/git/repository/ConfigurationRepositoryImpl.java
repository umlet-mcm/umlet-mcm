package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.util.PathUtils;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.*;
import at.ac.tuwien.model.change.management.git.operation.ConfigurationIOManager;
import at.ac.tuwien.model.change.management.git.operation.RepositoryManager;
import at.ac.tuwien.model.change.management.git.util.RepositoryContents;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import at.ac.tuwien.model.change.management.git.util.VersionControlUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class ConfigurationRepositoryImpl implements ConfigurationRepository {

    private final ConfigurationIOManager configurationIoManager;
    private final RepositoryManager repositoryManager;

    @Override
    public void createConfiguration(@NonNull String name) throws RepositoryAlreadyExistsException {
        log.debug("Creating repository for configuration '{}'.", name);
        repositoryManager.consumeRepository(name, repository -> {
            try {
                if (RepositoryUtils.repositoryExists(repository)) {
                    throw new RepositoryAlreadyExistsException("Repository for configuration '" + name + "' already exists.");
                }
                VersionControlUtils.initRepository(repository);
                log.info("Created repository for configuration '{}'.", name);
            } catch (GitAPIException e) {
                throw new RepositoryCreateException("Could not initialize repository for configuration '" + name + "'.", e);
            }
        });
    }

    @Override
    public Optional<Configuration> findConfigurationByName(@NonNull String name) {
        log.debug("Finding configuration '{}'.", name);
        return repositoryManager.withRepository(name, repository -> {
            if (!RepositoryUtils.repositoryExists(repository)) {
                log.warn("Repository for configuration '{}' could not be found.", name);
                return Optional.empty();
            }
            log.debug("Found repository for configuration '{}'.", name);
            return Optional.of(configurationIoManager.readConfigurationFromRepository(repository, Constants.HEAD));
        });
    }

    @Override
    public List<Configuration> findAllConfigurations() {
        log.debug("Searching all repositories for configurations.");
        return repositoryManager.withAllRepositories(repositories -> {
            var configurations = repositories.stream()
                    .map(repository -> configurationIoManager.readConfigurationFromRepository(repository, Constants.HEAD))
                    .toList();
            log.debug("Found {} configurations in repositories.", configurations.size());
            return configurations;
        });
    }

    @Override
    public Configuration saveConfiguration(@NonNull Configuration configuration) throws RepositoryDoesNotExistException {
        log.debug("Saving configuration '{}'.", configuration.getName());
        return repositoryManager.withRepository(configuration.getName(), repository -> {
            try {
                if (!RepositoryUtils.repositoryExists(repository)) {
                    throw new RepositoryDoesNotExistException("Repository for configuration '" + configuration.getName() + "' does not exist.");
                }
                configurationIoManager.clearConfigurationRepository(repository);
                var repositoryContents = configurationIoManager.writeConfigurationToRepository(repository, configuration);
                VersionControlUtils.stageRepositoryContents(repository, repositoryContents);
                VersionControlUtils.commitRepository(
                        repository, generateCommitMessage(configuration.getName(), repositoryContents), true
                );
                log.info("Saved configuration '{}' to repository.", configuration.getName());
                return configurationIoManager.readConfigurationFromRepository(repository, Constants.HEAD);
            } catch (GitAPIException e) {
                throw new RepositorySaveException("Failed to save configuration '" + configuration.getName() + "'.", e);
            }
        });
    }

    @Override
    public void deleteConfiguration(@NonNull String name) {
        log.debug("Deleting repository for configuration '{}'.", name);
        repositoryManager.consumeRepository(name, repository -> {
            try {
                var pathToWorkDir = repository.getWorkTree().toPath();
                if (PathUtils.deleteFilesRecursively(pathToWorkDir)) {
                    log.info("Deleted repository for configuration '{}'", name);
                } else {
                    log.warn("Could not delete repository for configuration '{}', because it does not exist", name);
                }
            } catch (IOException e) {
                throw new RepositoryDeleteException("Failed to delete repository for configuration '" + name + "'.", e);
            }
        });
    }

    private String generateCommitMessage(String name, RepositoryContents<?> repositoryContents) {
        return String.format("Update '%s' with %d models, %d nodes and %d relations",
                name, repositoryContents.models().size(), repositoryContents.nodes().size(), repositoryContents.relations().size());
    }
}
