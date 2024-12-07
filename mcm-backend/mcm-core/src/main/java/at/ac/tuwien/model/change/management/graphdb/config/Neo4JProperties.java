package at.ac.tuwien.model.change.management.graphdb.config;

import lombok.Getter;
import lombok.Setter;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "app.neo4j")
@Getter
@Setter
public class Neo4JProperties {

    private Path databasePath = Path.of("/tmp/mcm/graphdb");
    private File pluginPath = new File(databasePath.toString() + "/plugins");
    private File configsPath = new File(databasePath.toString() + "/conf");
    private Path relativeExportsPath = Path.of("/exports");
    private Path exportsPath = Path.of(databasePath.toString() + relativeExportsPath);
    private String uri = "bolt://localhost:7687";
    private String username = "neo4j";
    private String password = "password";
    private AuthToken authToken = AuthTokens.basic(username, password);
}
