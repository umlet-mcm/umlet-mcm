package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.*;
import at.ac.tuwien.model.change.management.git.util.RepositoryAdapter;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GitComponent
@RequiredArgsConstructor
public class ConfigurationRepositoryImpl implements ConfigurationRepository {

    private final RepositoryAdapter repositoryAdapter;

    @Override
    public Configuration create(Configuration configuration) {
        return repositoryAdapter.withRepository(configuration.getName(), false, repository -> {
            if (!RepositoryUtils.repositoryExists(repository)) {
                throw new ConfigurationAlreadyExistsException("Could not create configuration '" + configuration.getName() + "', because it already exists");
            }

            try {
                repository.create();
                // TODO: write configuration to repo worktree
                log.info("Created configuration '{}'", configuration.getName());
                return configuration;
            } catch (IOException e) {
                throw new ConfigurationCreateException("Failed to create configuration '" + configuration.getName() + "'", e);
            }
        });
    }


    @Override
    public Configuration update(Configuration configuration) {
        return repositoryAdapter.withGit(configuration.getName(), git -> {
            try {
                var headCommitVersion = RepositoryUtils.headCommitHash(git.getRepository());
                if (configuration.getVersion().equals(headCommitVersion)) {
                    // TODO: write configuration to repo worktree
                    var commitMessage = "Update for models: " + configuration.getModels().stream().map(Model::getId).collect(Collectors.joining(", "));
                    git.commit().setMessage(commitMessage).call();
                }
                return configuration;
            } catch (IOException e) {
                throw new ConfigurationReadException("Could not update configuration '" + configuration.getName() + "' due to failure to read its current repository state", e);
            } catch(GitAPIException e) {
                throw new ConfigurationUpdateException("Failed to update configuration '" + configuration.getName() + "'", e);
            }
        });
    }

    @Override
    public void delete(String name) {

    }

    @Override
    public Configuration findConfigurationByName(String name) {
        return null;
    }

    @Override
    public List<Configuration> findAll() {
        return List.of();
    }

    @Override
    public List<DiffEntry> diff(Configuration configuration, String oldCommitHash, String newCommitHash) {
        return List.of();
    }
}
