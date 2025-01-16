package at.ac.tuwien.model.change.management.graphdb.dao;

import at.ac.tuwien.model.change.management.graphdb.config.Neo4JProperties;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import at.ac.tuwien.model.change.management.graphdb.exceptions.InvalidQueryException;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for executing raw queries on the Neo4j database
 */
@Service
public class RawNeo4jService {
    /* The Neo4j driver to database */
    @Autowired
    private Driver neo4jDriver;
    @Autowired
    private Neo4JProperties properties;

    /**
     * Executes a raw query on the Neo4j database
     * @param cypherQuery The query to execute
     * @return The result of the query
     */
    public List<Map<String, Object>> executeRawQuery(String cypherQuery) {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypherQuery);
            List<Map<String, Object>> rawData = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                rawData.add(record.asMap());
            }
            return rawData;
        } catch (ClientException e) {
            throw new InvalidQueryException("Invalid query: " + e.getMessage());
        }
    }

    /**
     * Generates a CSV file from the Neo4j database and stores it inside the database folder
     * @param fileName The name of the CSV file
     */
    public void generateCSV(String fileName) {
        try (Session session = neo4jDriver.session()) {
            String query = "CALL apoc.export.csv.all('" + properties.getRelativeExportsPath().toString() + "/" + fileName + ".csv', {})";
            session.run(query);
        } catch (ClientException e) {
            throw new InvalidQueryException("Error Exporting to CSV! " + e.getMessage());
        }
    }

    /**
     * Generates a CSV file from a custom query and stores it inside the database folder
     * @param fileName The name of the CSV file
     * @param query The custom query which contains subgraph to export
     */
    public void generateQueryCSV(String fileName, String query) {
        try (Session session = neo4jDriver.session()) {
            String command = "WITH \"" + query + "\" AS query\n" +
                    "CALL apoc.export.csv.query(query, \"" + properties.getRelativeExportsPath().toString() + "/" + fileName + ".csv\", {})\n" +
                    "YIELD file, source, format, nodes, relationships, properties, time, rows, batchSize, batches, done, data\n" +
                    "RETURN file, source, format, nodes, relationships, properties, time, rows, batchSize, batches, done, data;";
            session.run(command);
        } catch (ClientException e) {
            throw new InvalidQueryException("Error Exporting to CSV! " + e.getMessage());
        }
    }

    /**
     * Downloads a CSV file from the Neo4j database folder
     * @param fileName The name of the CSV file
     * @return The CSV file as an InputStreamResource
     */
    public ByteArrayResource downloadCSV(String fileName) {
        try {
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(Paths.get(properties.getExportsPath().toString() + "/" + fileName + ".csv")));
            return resource;
        } catch (IOException e) {
            throw new InvalidQueryException("Error downloading CSV! " + e.getMessage());
        }
    }

    /**
     * Clears the Neo4j database, removing everything and detaching all nodes from their relations
     */
    public void clearDatabase() {
        try (Session session = neo4jDriver.session()) {
            String query = "MATCH (n) DETACH DELETE n";
            session.run(query);
        } catch (ClientException e) {
            throw new InvalidQueryException("Error clearing database! " + e.getMessage());
        }
    }

    /**
     * Deletes all nodes except those that are part of the subgraph returned by the query.
     * The query must return a column of type Node or Element ID.
     * @param query The query containing the subgraph to keep
     */
    public void getOnlyQuerySubgraph(String query) {
        try (Session session = neo4jDriver.session()) {
            // Step 1: Execute the query to get the IDs of the nodes to keep
            Result result = session.run(query);
            List<String> idsToKeep = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                // Check if there is at least one column
                if (record.size() == 0) {
                    throw new InvalidQueryException("Query must return at least one column!");
                }
                // Check whether the value in a column is String (elementId) or Entity (Node)
                if(record.get(0).type().name().equals("NODE"))
                    idsToKeep.add(record.get(0).asEntity().elementId());
                else if (record.get(0).type().name().equals("STRING")) {
                    try {
                        // Check if the string is a valid UUID
                        String regex = ".*:(.*):.*";
                        String type = record.get(0).asString().replaceAll(regex, "$1");
                        UUID.fromString(type);
                        idsToKeep.add(record.get(0).asString());
                    } catch (IllegalArgumentException e) {
                        throw new InvalidQueryException("Query must return a column of type Node or Element ID! The column contains String but not an Element ID!");
                    }
                }

                else
                    throw new InvalidQueryException("Query must return a column of type Node or Element ID!");
            }

            // Needs at least one row to be kept
            if(idsToKeep.size() == 0)
                throw new InvalidQueryException("Query must return at least one row!");

            // Step 2: Delete all nodes except those with the IDs in idsToKeep
            String deleteQuery = "MATCH (n) WHERE NOT elementId(n) IN $idsToKeep DETACH DELETE n";
            session.run(deleteQuery, Map.of("idsToKeep", idsToKeep));
        } catch (ClientException e) {
            throw new InvalidQueryException("Error performing query! " + e.getMessage());
        }
    }
}
