package at.ac.tuwien.model.change.management.git.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "app.git")
@Getter
@Setter
public class GitProperties {

    private Path repositoryPath = Path.of("/tmp/mcm/git");
}
