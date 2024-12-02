package at.ac.tuwien.model.change.management.graphdb.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import javax.annotation.PreDestroy;
import java.time.Duration;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

@Slf4j
@Configuration
@EnableNeo4jRepositories(basePackages = "at.ac.tuwien.model.change.management")
public class Neo4JConfig {

    private DatabaseManagementService managementService;

    @Bean
    public DatabaseManagementService databaseManagementService(Neo4JProperties properties) {
        managementService = new DatabaseManagementServiceBuilder(properties.getDatabasePath())
                .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60))
                .setConfig(BoltConnector.enabled, true)
                .build();
        return managementService;
    }

    @Bean
    public GraphDatabaseService graphDatabaseService(DatabaseManagementService managementService) {
        var graphDB = managementService.database(DEFAULT_DATABASE_NAME);
        log.info("Graph database '{}' is available: {}", graphDB.databaseName(), graphDB.isAvailable());
        return graphDB;
    }

    @PreDestroy
    public void shutdownDatabaseManagementService() {
        if (managementService != null) {
            managementService.shutdown();
        }
    }

    @Bean
    public Driver neo4jDriver(Neo4JProperties properties) {
        return GraphDatabase.driver(properties.getUri(), properties.getAuthToken());
    }
}
