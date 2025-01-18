package at.ac.tuwien.model.change.management.git.config;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration properties for the Git repository.
 * Currently configurable properties;
 * - repositories: the path where the repositories are stored, /tmp/mcm/git by default
 * - encoding: the encoding used for reading and writing files in the repository, UTF-8 by default
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "app.git")
public class GitProperties {

    @NonNull private Path repositories = Path.of("/tmp/mcm/git");
    @NonNull private Charset encoding = StandardCharsets.UTF_8;

    @PostConstruct
    public void init() {
        validatePath();
        log.debug("Using repository path '{}'.", this.repositories);
    }

    private void validatePath() {
        try {
            if (Files.notExists(repositories)) {
                Files.createDirectories(repositories);
            } else if (!Files.isDirectory(repositories)) {
                throw new RepositoryAccessException("Repository path '" + repositories + "' exists, but is not a directory. "
                        + "Please make sure the path is correctly configured in the 'app.git.repositories' property");
            } else if (! Files.isWritable(repositories)) {
                throw new RepositoryAccessException("Repository path '" + repositories + "' is not writable. "
                        + "Please make sure the path is correctly configured in the 'app.git.repositories' property");
            }
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not create repository directory '" + repositories + "'", e);
        }
    }
}
