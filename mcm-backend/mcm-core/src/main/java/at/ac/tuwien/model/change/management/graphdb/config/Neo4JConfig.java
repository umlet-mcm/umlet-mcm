package at.ac.tuwien.model.change.management.graphdb.config;

import at.ac.tuwien.model.change.management.graphdb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

@Slf4j
@Configuration
@EnableNeo4jRepositories(basePackages = "at.ac.tuwien.model.change.management")
public class Neo4JConfig {

    private DatabaseManagementService managementService;

    private final Neo4JProperties properties;

    public Neo4JConfig(Neo4JProperties properties) {
        this.properties = properties;
    }

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

    /**
     * Copy the plugins and configs to the graphDB directory at the startup
     */
    @PostConstruct
    public void copyFiles() {
        try {
            /* Copy the resources to the graphDB directory */
            for(val plugin : properties.getPlugins()) {
                Utils.copyFromJar(new ClassPathResource("graphDB/plugins/" + plugin), new File(properties.getPluginPath() + "/" + plugin));
            }
            for(val config : properties.getConfigs()) {
                Utils.copyFromJar(new ClassPathResource("graphDB/configs/" + config), new File(properties.getConfigsPath() + "/" + config));
            }
            log.info("Copied resources to the graphDB directory");

            /* Create the exports directory */
            Files.createDirectories(properties.getExportsPath());
            log.info("Created exports directory");
        } catch (IOException e) {
            log.error("Error copying resources", e);
        }
    }
}
