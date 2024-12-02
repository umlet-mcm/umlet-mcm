package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Node;

import java.util.List;
import java.util.Map;

/**
 * Service for executing queries on the graph database
 */
public interface GraphDBService {
    /**
     * Saves a node to the graph database
     * @param node The node to save
     * @return The saved node
     */
    Node loadNode(Node node);

    /**
     * Executes a generic query on the graph database
     * @param query The query to execute
     * @return The result of the query
     */
    List<Map<String,Object>> executeQuery(String query);
}
