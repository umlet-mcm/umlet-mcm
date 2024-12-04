package at.ac.tuwien.model.change.management.git.config;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Getter
@ConfigurationProperties(prefix = "app.git")
public class GitProperties {

    private final Path repositoryPath;

    public GitProperties(Path repositoryPath) {
        this.repositoryPath = repositoryPath != null
                ? repositoryPath
                : Path.of("/tmp/mcm/git");
        validatePath();
        log.debug("Using repository path '{}'.", this.repositoryPath);
    }

    private void validatePath() {
        try {
            if (!Files.exists(repositoryPath)) {
                Files.createDirectories(repositoryPath);
            } else if (!Files.isDirectory(repositoryPath)) {
                throw new RepositoryAccessException("Repository path '" + repositoryPath + "' is not a directory. "
                        + "Please make sure the path is correctly configured in the 'app.git.repositoryPath' property");
            }
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not create repository directory '" + repositoryPath + "'", e);
        }
    }
}
