package at.ac.tuwien.model.change.management.core.mapper.neo4j;


import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import lombok.val;
import org.neo4j.driver.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The currently used implementation of the NodeEntityMapper
 */
@Component
public class NodeEntityMapperImpl implements NodeEntityMapper {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    @Lazy
    private RelationEntityMapper relationEntityMapper;

    @Autowired
    private CycleAvoidingMappingContext context;

    @Override
    public NodeEntity toEntity(Node node) {
        if(node == null) {
            return null;
        }

        if(context.getMappedInstance(node, NodeEntity.class) != null) {
            return (NodeEntity) context.getMappedInstance(node, NodeEntity.class);
        }

        NodeEntity nodeEntity = new NodeEntity();

        context.storeMappedInstance(node, nodeEntity);

        // If node already exists inside the database, set the ID
        // Becomes update instead of insert
        if (node.getId() != null) {
            nodeEntity.setGeneratedID(node.getId());
        }

        // Set the name of the node
        nodeEntity.setName(node.getTitle());

        // Set description of the node
        nodeEntity.setDescription(node.getDescription());

        // Set the type of the node
        nodeEntity.setType(node.getElementType());

        if(node.getPprType() != null) {
            nodeEntity.setPprType(node.getPprType());
        }

        // Set the relations of the node
        if(node.getRelations() != null) {
            nodeEntity.setRelations(node.getRelations().stream().map(relation -> relationEntityMapper.toEntity(relation)).collect(Collectors.toSet()));
        }

        // Set the properties of the node
        if (node.getMcmAttributes() != null) {
            val mcmAttributes = new HashMap<String, Value>() {{
                node.getMcmAttributes().forEach((key, value) -> put(key, Neo4jValueConverter.convertObject(value)));
            }};
            nodeEntity.setProperties(mcmAttributes);
        }

        // Set the attributes of the node
        nodeEntity.setUmletProperties(node.getUmletAttributes());

        // Set the tags of the node
        if(node.getTags() != null)
            nodeEntity.setTags(new HashSet<>(node.getTags()));

        // Set the position of the node
        nodeEntity.setPosition(positionMapper.toGraphProperties(node.getUmletPosition()));

        // Return the node entity
        return nodeEntity;
    }

    @Override
    public Node fromEntity(NodeEntity nodeEntity) {
        if(nodeEntity == null) {
            return null;
        }

        // To prevent cycles, check if the node entity is already mapped
        if(context.getMappedInstance(nodeEntity, Node.class) != null) {
            return (Node) context.getMappedInstance(nodeEntity, Node.class);
        }

        Node node = new Node();

        // Store the mapping because it has not been mapped yet
        context.storeMappedInstance(nodeEntity, node);

        // Set an assigned ID to the node
        if(nodeEntity.getGeneratedID() != null) {
            node.setId(nodeEntity.getGeneratedID());
        }

        // Set the title of the node
        node.setTitle(nodeEntity.getName());

        // Set the description of the node
        node.setDescription(nodeEntity.getDescription());

        // Map relations back to the Node
        node.setRelations(nodeEntity.getRelations().stream().map(relation -> relationEntityMapper.fromEntity(relation)).collect(Collectors.toSet()));

        // Set the type of the node
        node.setElementType(nodeEntity.getType());

        // Set the PPR type of the node
        if(nodeEntity.getPprType() != null) {
            node.setPprType(nodeEntity.getPprType());
        }

        // Maps the properties of the node entity back to node model
        val mcmAttributes  = new LinkedHashMap<String,Object>() {{
            nodeEntity.getProperties().forEach((key, value) -> put(key, Neo4jValueConverter.convertValue((Value) value)));
        }};
        node.setMcmAttributes( mcmAttributes );
        node.setUmletAttributes( new LinkedHashMap<>(nodeEntity.getUmletProperties()) );

        // Set the tags of the node
        node.setTags(new ArrayList<>(nodeEntity.getTags()));

        // Set the position of the node
        node.setUmletPosition(positionMapper.toLocation(nodeEntity.getPosition()));

        // Return the node
        return node;
    }
}
