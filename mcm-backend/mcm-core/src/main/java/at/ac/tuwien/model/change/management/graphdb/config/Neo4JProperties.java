package at.ac.tuwien.model.change.management.graphdb.config;

import lombok.Getter;
import lombok.Setter;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.neo4j")
@Getter
@Setter
public class Neo4JProperties {
    // Path to the folder where the database is stored
    private Path databasePath = Path.of("/tmp/mcm/graphdb");

    // Path to the folder where the plugins are stored for the DB
    private File pluginPath = new File(databasePath + "/plugins");

    // Path to the folder where the configurations are stored for the DB
    private File configsPath = new File(databasePath + "/conf");

    // Path to the folder where the exports are stored
    private Path relativeExportsPath = Path.of("/exports");
    private Path exportsPath = Path.of(databasePath.toString() + relativeExportsPath);

    private String uri = "bolt://localhost:7687";
    private String username = "neo4j";
    private String password = "password";
    private AuthToken authToken = AuthTokens.basic(username, password);

    // List of plugins to be installed to the DB
    private List<String> plugins = List.of("apoc-5.25.1-core.jar","neo4j-graph-data-science-2.12.0.jar");

    // List of configuration files to be applied to the DB
    private List<String> configs = List.of("neo4j.conf", "apoc.conf");
}
