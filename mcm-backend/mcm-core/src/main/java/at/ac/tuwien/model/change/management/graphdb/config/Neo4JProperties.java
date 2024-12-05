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
    private File pluginPath = new File("/tmp/mcm/graphdb/plugins");
    private File configsPath = new File("/tmp/mcm/graphdb/conf");
    private String uri = "bolt://localhost:7687";
    private String username = "neo4j";
    private String password = "password";
    private AuthToken authToken = AuthTokens.basic(username, password);
}
