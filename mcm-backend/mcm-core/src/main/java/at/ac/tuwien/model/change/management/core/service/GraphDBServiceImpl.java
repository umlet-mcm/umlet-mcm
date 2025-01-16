package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.*;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.updater.ConfigurationUpdater;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.updater.NodeUpdater;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.graphdb.dao.ConfigurationEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.ModelEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.NodeEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.RawNeo4jService;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import at.ac.tuwien.model.change.management.graphdb.exceptions.InvalidQueryException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.StringValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphDBServiceImpl implements GraphDBService {

    /* DAOs */
    private final NodeEntityDAO nodeEntityDAO;
    private final ConfigurationEntityDAO configurationEntityDAO;
    private final ModelEntityDAO modelEntityDAO;

    /* Mappers */
    private final NodeEntityMapper nodeEntityMapper;
    private final ConfigurationEntityMapper configurationEntityMapper;
    private final ModelEntityMapper modelEntityMapper;

    /* Updater */
    private final ConfigurationUpdater configurationUpdater;
    private final NodeUpdater nodeUpdater;

    private final RawNeo4jService rawNeo4jService;

    @Lazy @Autowired
    private ConfigurationService configurationService;
    @Lazy @Autowired
    private UxfService uxfService;

    @Override
    public Node loadNode(@NonNull Node node) {
        var nodeEntity = nodeEntityDAO.save(nodeEntityMapper.toEntity(node));
        return nodeEntityMapper.fromEntity(nodeEntity);
    }

    @Override
    public Node getNode(@NonNull String id) {
        val node = nodeEntityDAO.findById(id).orElse(null);
        return nodeEntityMapper.fromEntity(node);
    }

    @Override
    public List<Node> getNodes() {
        return nodeEntityDAO.findAll().stream().map(nodeEntityMapper::fromEntity).toList();
    }

    @Override
    public void deleteNode(@NonNull String id) {
        nodeEntityDAO.deleteById(id);
    }

    @Override
    public List<Node> getPredecessors(String nodeID) {
        return nodeEntityDAO.getPredecessors(nodeID).stream().map(nodeEntityMapper::fromEntity).toList();
    }

    @Override
    public Node sumUpAttribute(String nodeID, String attributeName) {
        // Get the predecessors
        val predecessors = nodeEntityDAO.getPredecessors(nodeID);

        // Get the node
        val node = nodeEntityDAO.findById(nodeID).orElse(null);

        // If the node does not exist, return null
        if(node == null) {
            return null;
        }

        // Sum up the attribute
        var sumUp = 0.0;
        for(NodeEntity predecessor : predecessors) {
            if(predecessor.getProperties().containsKey(attributeName)) {
                val property = predecessor.getProperties().get(attributeName);
                sumUp += Double.parseDouble(property.toString());
            }
        }

        // Set the attribute
        node.getProperties().put(attributeName, Neo4jValueConverter.convertObject(sumUp));

        // Save the node
        return nodeEntityMapper.fromEntity(nodeEntityDAO.save(node));
    }

    @Override
    public Configuration loadConfiguration(@NonNull Configuration configuration) {
        val configurationEntity = configurationEntityDAO.save(configurationEntityMapper.toEntity(configuration));
        return configurationEntityMapper.fromEntity(configurationEntity);
    }

    @Override
    public Configuration loadConfiguration(String name) {
        // Get the configuration else throw an ConfigurationNotFoundException
        val configuration = configurationService.getConfigurationByName(name);

        // Clear the database
        clearDatabase();

        // Save the configuration
        configurationEntityDAO.save(configurationEntityMapper.toEntity(configuration));

        log.info("Loaded configuration '{}' into graph database.", configuration.getName());

        // Return the configuration
        return configuration;
    }

    @Override
    public Configuration getConfiguration(@NonNull String id) {
        val configuration = configurationEntityDAO.findById(id).orElse(null);
        return configurationEntityMapper.fromEntity(configuration);
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configurationEntityDAO.findAll().stream().map(configurationEntityMapper::fromEntity).toList();
    }

    @Override
    public Boolean saveDBToRepository() {
        // Obtain configurations
        val configurations = getConfigurations();

        // No configuration is loaded
        if(configurations.isEmpty()) {
            return false;
        }

        // Get the first configuration - should be just one
        val configuration = configurations.get(0);

        // Update the configuration
        val oldConfiguration = configurationService.getConfigurationByName(configuration.getName());
        configurationUpdater.updateConfiguration(configuration, oldConfiguration);

        // Update the configuration
        // The old configuration is now updated
        configurationService.updateConfiguration(oldConfiguration);

        return true;
    }

    @Override
    public void deleteConfiguration(@NonNull String id) {
        configurationEntityDAO.deleteById(id);
    }

    @Override
    public Model loadModel(@NonNull Model model) {
        val modelEntity = modelEntityDAO.save(modelEntityMapper.toEntity(model));
        return modelEntityMapper.fromEntity(modelEntity);
    }

    @Override
    public Model getModel(@NonNull String id) {
        val model = modelEntityDAO.findById(id).orElse(null);
        return modelEntityMapper.fromEntity(model);
    }

    @Override
    public List<Model> getModels() {
        return modelEntityDAO.findAll().stream().map(modelEntityMapper::fromEntity).toList();
    }

    @Override
    public void deleteModel(@NonNull String id) {
        modelEntityDAO.deleteById(id);
    }

    @Override
    public String executeQuery(String query) {
        val response = rawNeo4jService.executeRawQuery(query);
        // Register the custom serializer with Gson
        Gson gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(Value.class, new Neo4jTypeAdapter())
                .create();
        // Convert to JSON
        val convert = gson.toJson(response);
        return convert;
    }

    public ByteArrayResource generateCSV(String fileName) {
        rawNeo4jService.generateCSV(fileName);
        return rawNeo4jService.downloadCSV(fileName);
    }

    public ByteArrayResource generateQueryCSV(String fileName, String query) {
        rawNeo4jService.generateQueryCSV(fileName, query);
        return rawNeo4jService.downloadCSV(fileName);
    }

    @Override
    public ByteArrayResource generateQueryUXF(String query) {
        // Save the database temporarily
        val configurations = getConfigurations();
        if(configurations.isEmpty()) {
            throw new InvalidQueryException("No configuration is loaded in the DB!");
        }
        val configuration = configurations.get(0);

        String uxfFile;

        try{
            // Convert the database to contain only the subgraph
            rawNeo4jService.getOnlyQuerySubgraph(query);
            log.info("Successfully converted the database to contain only the subgraph.");

            // Get the nodes that remain
            val nodes = getNodes();

            // Update the nodes from repository
            val updatedNodes = updateNodes(nodes, configuration.getName());

            // Create a new model out of them
            val model = new Model();
            val nodesSet = new HashSet<Node>(updatedNodes);
            model.setNodes(nodesSet);

            // Export the nodes to a file
            uxfFile = uxfService.exportModel(model);
            log.info("Successfully exported the subgraph to a UXF file.");
        } catch (UxfException e) {
            throw new InvalidQueryException(e.getMessage());
        } finally {
            // Restore the database even if error occurs
            clearDatabase();
            loadConfiguration(configuration);
            log.info("Successfully restored the database.");
        }
        return new ByteArrayResource(uxfFile.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Updates the nodes with the ones from the repository
     * @param nodes The nodes to update
     * @param configName The name of the configuration to get the nodes from
     * @return The updated nodes
     */
    private List<Node> updateNodes(List<Node> nodes, String configName) {
        // Get the configuration
        val configuration = configurationService.getConfigurationByName(configName);

        // Get all nodes to a hash map
        val nodesMap = new HashMap<String, Node>();
        for(Model model : configuration.getModels()) {
            for(Node repositoryNode : model.getNodes()) {
                nodesMap.put(repositoryNode.getId(), repositoryNode);
            }
        }

        // Update the nodes
        val updatedNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if(nodesMap.containsKey(node.getId())) {
                // Update the node
                val repositoryNode = nodesMap.get(node.getId());
                nodeUpdater.updateNode(node, repositoryNode);
                repositoryNode.setId(null);
                repositoryNode.setMcmModelId(null);
                updatedNodes.add(repositoryNode);

                // Delete relation IDs
                for (Relation relation : repositoryNode.getRelations()) {
                    relation.setId(null);
                    relation.setMcmModelId(null);
                }
            }
        }
        return updatedNodes;
    }

    @Override
    public void clearDatabase() {
        rawNeo4jService.clearDatabase();
        log.info("Cleared the graph database.");
    }
}
