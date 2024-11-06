package at.ac.tuwien.model.change.management.git.repository;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.PersistenceException;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.nio.file.Path;

@Slf4j
@GitComponent
@RequiredArgsConstructor
public class GitConfigurationRepository implements ConfigurationRepository {

    private final GitProperties properties;

    private boolean gitRepositoryExists(Path path) {
        return path.resolve(".git").toFile().exists();
    }

    @Override
    public Configuration save(Configuration configuration) {
        var repositoryPath = properties.getRepositoryPath().resolve(configuration.getName());

        if (gitRepositoryExists(repositoryPath)) {
            log.error("Git repository for configuration '{}' already exists and cannot be created", configuration.getName());
            throw new ConfigurationAlreadyExistsException("Configuration '" + configuration.getName() + "' already exists");
        }

        try (Git ignored = Git.init().setDirectory(repositoryPath.toFile()).call()) {
            log.info("Successfully created repository for configuration '{}'", configuration.getName());
            return configuration;
        } catch (GitAPIException e) {
            log.error("Failed to initialize git repository for Configuration '{}'", configuration.getName(), e);
            throw new PersistenceException("Failed to add configuration '" + configuration.getName() + "'", e);
        }
    }
}
