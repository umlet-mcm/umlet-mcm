package at.ac.tuwien.model.change.management.core.mapper;


import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The currently used implementation of the NodeEntityMapper
 */
@Component
public class NodeEntityMapperImpl implements NodeEntityMapper {
    private final static String TAGS = "tags";

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    @Lazy
    private RelationEntityMapper relationEntityMapper;

    @Override
    public NodeEntity toEntity(Node node) {
        NodeEntity nodeEntity = new NodeEntity();

        // If node already exists inside the database, set the ID
        // Becomes update instead of insert
        if (node.getId() != null) {
            nodeEntity.setGeneratedID(Long.parseLong(node.getId()));
        }

        // Set the name of the node
        nodeEntity.setName(node.getDescription());

        // Set the type of the node
        nodeEntity.setType(node.getElementType());

        // Set the relations of the node
        if(node.getRelations() != null) {
            nodeEntity.setRelations(node.getRelations().stream().map(relation -> relationEntityMapper.toEntity(relation)).collect(Collectors.toSet()));
        }

        // Set the properties of the node, i.e. the attributes
        if(node.getUmletAttributes() != null) {
            nodeEntity.setProperties(node.getUmletAttributes());
        }

        // Set the tags of the node
        if(node.getMcmAttributes() != null && node.getMcmAttributes().containsKey(TAGS) && node.getMcmAttributes().get(TAGS) instanceof Set) {
            nodeEntity.setTags((Set<String>) node.getMcmAttributes().get(TAGS));
        }

        // Set the position of the node
        nodeEntity.setPosition(positionMapper.toGraphProperties(node.getUmletPosition()));

        // Return the node entity
        return nodeEntity;
    }

    @Override
    public Node fromEntity(NodeEntity nodeEntity) {
        Node node = new Node();

        // Set an assigned ID to the node
        node.setId(nodeEntity.getGeneratedID().toString());

        // Set the text of the node
        node.setDescription(nodeEntity.getName());

        // Map relations back to the Node
        node.setRelations(nodeEntity.getRelations().stream().map(relation -> new Relation()).collect(Collectors.toSet()));

        // Set the type of the node
        node.setElementType(nodeEntity.getType());

        // Maps the properties of the node entity back to node model
        node.setUmletAttributes(nodeEntity.getProperties());

        // Set the tags of the node
        if(node.getMcmAttributes() != null) {
            node.getMcmAttributes().put(TAGS, nodeEntity.getTags());
        } else {
            Map<String, Object> mcmAttributes = new HashMap<>();
            mcmAttributes.put(TAGS, nodeEntity.getTags());
            node.setMcmAttributes(mcmAttributes);
        }

        // Set the position of the node
        node.setUmletPosition(positionMapper.toLocation(nodeEntity.getPosition()));

        // Return the node
        return node;
    }
}
