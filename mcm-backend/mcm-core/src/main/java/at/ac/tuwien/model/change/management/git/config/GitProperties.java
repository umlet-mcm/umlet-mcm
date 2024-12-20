package at.ac.tuwien.model.change.management.git.config;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "app.git")
public class GitProperties {

    private Path repositories = Path.of("/tmp/mcm/git");

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
