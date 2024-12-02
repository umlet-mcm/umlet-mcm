package at.ac.tuwien.model.change.management.graphdb.dao;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for executing raw queries on the Neo4j database
 */
@Service
public class RawNeo4jService {
    /* The Neo4j driver to database */
    @Autowired
    private Driver neo4jDriver;

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
        }
    }
}
