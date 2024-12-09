package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.mapper.neo4j.ConfigurationEntityMapper;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.ModelEntityMapper;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.NodeEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.dao.ConfigurationEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.ModelEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.NodeEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.RawNeo4jService;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.values.storable.NumberValue;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GraphDBServiceImpl implements GraphDBService {

    /* DAOs */
    private final NodeEntityDAO nodeEntityDAO;
    private final ConfigurationEntityDAO configurationEntityDAO;
    private final ModelEntityDAO modelEntityDAO;

    /* Mappers */
    private final NodeEntityMapper nodeEntityMapper;
    private final ConfigurationEntityMapper configurationEntityMapper;
    private final ModelEntityMapper modelEntityMapper;

    private final RawNeo4jService rawNeo4jService;

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
                if (property instanceof StringValue) {
                    sumUp += Double.parseDouble(((StringValue) property).asString());
                } else if (property instanceof IntegerValue) {
                    sumUp += ((IntegerValue) property).asDouble();
                }
            }
        }

        // Set the attribute
        node.getProperties().put(attributeName, sumUp);

        // Save the node
        return nodeEntityMapper.fromEntity(nodeEntityDAO.save(node));
    }

    @Override
    public Configuration loadConfiguration(@NonNull Configuration configuration) {
        val configurationEntity = configurationEntityDAO.save(configurationEntityMapper.toEntity(configuration));
        return configurationEntityMapper.fromEntity(configurationEntity);
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
    public List<Map<String,Object>> executeQuery(String query) {
        val response = rawNeo4jService.executeRawQuery(query);
        return response;
    }

    public ByteArrayResource generateCSV(String fileName) {
        rawNeo4jService.generateCSV(fileName);
        return rawNeo4jService.downloadCSV(fileName);
    }

    @Override
    public void clearDatabase() {
        rawNeo4jService.clearDatabase();
    }
}
