package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import org.neo4j.procedure.Mode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;

import java.util.List;
import java.util.Map;

/**
 * Service for executing queries on the graph database
 */
public interface GraphDBService {
    /******** NODE PART ********/

    /**
     * Saves a node to the graph database
     * @param node The node to save
     * @return The saved node
     */
    Node loadNode(Node node);

    /**
     * Retrieves a node from the graph database
     * @param id The id of the node to retrieve
     * @return The retrieved node
     */
    Node getNode(String id);

    /**
     * Retrieves all nodes from the graph database
     * @return The list of all nodes
     */
    List<Node> getNodes();

    /**
     * Deletes a node from the graph database
     * @param id The id of the node to delete
     */
    void deleteNode(String id);

    /**
     * Retrieves all predecessor nodes from the graph database
     * @param nodeID The node ID of whose predecessors to retrieve
     * @return The list of all nodes with outgoing relations
     */
    List<Node> getPredecessors(String nodeID);

    /**
     * Sum up one given attribute from all predecessors of a node
     * @param nodeID The node ID which will be summed up
     * @param attributeName The attribute name to sum up
     * @return The node containing the summed attribute
     */
    Node sumUpAttribute(String nodeID, String attributeName);


    /********** CONFIGURATION PART ********/

    /**
     * Loads a configuration to the graph database
     * @param configuration The configuration to load
     * @return The loaded configuration
     */
    Configuration loadConfiguration(Configuration configuration);

    /**
     * Loads a configuration to the graph database
     * @param name The name of the configuration to load
     * @return The loaded configuration
     * @throws ConfigurationNotFoundException if the configuration does not exist
     */
    Configuration loadConfiguration(String name);

    /**
     * Retrieves a configuration from the graph database
     * @param id The id of the configuration to retrieve
     * @return The retrieved configuration
     */
    Configuration getConfiguration(String id);

    /**
     * Retrieves all configurations from the graph database
     * @return The list of all configurations
     */
    List<Configuration> getConfigurations();

    /**
     * Saves the graph database to the repository
     * @return True if the save was successful, false otherwise
     */
    Boolean saveDBToRepository();

    /**
     * Deletes a configuration from the graph database
     * @param id The id of the configuration to delete
     */
    void deleteConfiguration(String id);


    /********** MODEL PART ********/

    /**
     * Loads a model to the graph database
     * @param model The model to load
     * @return The loaded model
     */
    Model loadModel(Model model);

    /**
     * Retrieves a model from the graph database
     * @param id The id of the model to retrieve
     * @return The retrieved model
     */
    Model getModel(String id);

    /**
     * Retrieves all models from the graph database
     * @return The list of all models
     */
    List<Model> getModels();

    /**
     * Deletes a model from the graph database
     * @param id The id of the model to delete
     */
    void deleteModel(String id);

    /******** GENERIC PART ********/

    /**
     * Executes a generic query on the graph database
     * @param query The query to execute
     * @return The result of the query in JSON format
     */
    String executeQuery(String query);

    /**
     * Generates a CSV file from the graph database
     * @param fileName The name of the CSV file
     * @return The CSV file as an InputStreamResource
     */
    ByteArrayResource generateCSV(String fileName);

    /**
     * Generates a CSV file from a custom query
     * @param fileName The name of the CSV file
     * @param query The custom query which contains subgraph to export
     * @return The CSV file as an InputStreamResource
     */
    ByteArrayResource generateQueryCSV(String fileName, String query);

    /**
     * Clears the graph database, removing all nodes, relations and attributes
     */
    void clearDatabase();
}
