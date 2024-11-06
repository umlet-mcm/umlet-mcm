package at.ac.tuwien.model.change.management.graphdb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "app.neo4j")
@Getter
@Setter
public class Neo4JProperties {

    private Path databasePath = Path.of("/tmp/mcm/graphdb");
}
