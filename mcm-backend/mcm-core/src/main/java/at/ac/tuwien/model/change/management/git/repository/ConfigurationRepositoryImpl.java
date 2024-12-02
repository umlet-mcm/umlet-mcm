package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.*;
import at.ac.tuwien.model.change.management.git.util.RepositoryManager;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@GitComponent
@RequiredArgsConstructor
public class ConfigurationRepositoryImpl implements ConfigurationRepository {

    private final RepositoryManager repositoryManager;

    @Override
    public Configuration create(Configuration configuration) {
        return repositoryManager.withRepository(configuration.getName(), false, repository -> {
            if (RepositoryUtils.repositoryExists(repository)) {
                throw new ConfigurationAlreadyExistsException("Could not create configuration '" + configuration.getName() + "', because it already exists");
            }

            try {
                repository.create();
                repositoryManager.writeConfigurationToRepository(configuration, repository);
                log.info("Created configuration '{}'", configuration.getName());
                return repositoryManager.readConfigurationFromRepository(repository);
            } catch (IOException e) {
                throw new ConfigurationCreateException("Failed to create configuration '" + configuration.getName() + "'", e);
            }
        });
    }


    @Override
    public Configuration update(Configuration configuration) {
        return repositoryManager.withGit(configuration.getName(), git -> {
            try {
                var updatedConfigurationVersion = Optional.ofNullable(configuration.getVersion())
                        .orElseThrow(() -> new ConfigurationUpdateException("Could not update configuration '" + configuration.getName() + "', because it has no version"));
                var headCommitVersion = RepositoryUtils.headCommitHash(git.getRepository());

                if (updatedConfigurationVersion.equals(headCommitVersion)) {
                    var commitMessage = "Update for models: " + configuration.getModels().stream().map(Model::getId).collect(Collectors.joining(", "));
                    var updatedRepositoryContents = repositoryManager.updateRepositoryWorkTree(configuration, git.getRepository());
                    repositoryManager.addRepositoryContents(git.getRepository(), updatedRepositoryContents);
                    git.commit().setMessage(commitMessage).call();
                    return repositoryManager.readConfigurationFromRepository(git.getRepository());
                } else {
                    // TODO: more sophisticated handling of this case
                    throw new ConfigurationUpdateException("Could not update configuration '" + configuration.getName() + "', because it is not at the latest version");
                }
            } catch (GitAPIException e) {
                throw new ConfigurationUpdateException("Failed to update configuration '" + configuration.getName() + "'", e);
            }
        });
    }

    @Override
    public void delete(String name) {
        // using withRepository instead of withGit, because deletions are usually supposed to be idempotent
        repositoryManager.withRepository(name, false, repository -> {
            try {
                if (!FileSystemUtils.deleteRecursively(repository.getWorkTree().toPath())) {
                    throw new ConfigurationDeleteException("Failed to delete configuration '" + name + "'");
                }
            } catch (IOException e) {
                throw new ConfigurationDeleteException("Failed to delete configuration '" + name + "'", e);
            }
        });
    }

    @Override
    public Configuration findConfigurationByName(String name) {
        return repositoryManager.readConfigurationFromRepository(name);
    }

    @Override
    public List<Configuration> findAll() {
        try (var files = Files.list(repositoryManager.gitRepositoriesPath())) {
            return files.map(Path::getFileName)
                    .map(Path::toString)
                    .map(repositoryName -> repositoryManager.withRepository(repositoryName, false, repository -> {
                        if (RepositoryUtils.repositoryExists(repository)) {
                            return repositoryManager.readConfigurationFromRepository(repositoryName);
                        }
                        return null;
                    }))
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to list all configurations", e);
        }
    }

    @Override
    public List<DiffEntry> diff(Configuration configuration, String oldCommitHash, String newCommitHash) {
        return List.of();
    }
}
