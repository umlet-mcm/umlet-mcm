package at.ac.tuwien.model.change.management.core.mapper;


import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * The currently used implementation of the NodeEntityMapper
 */
@Component
public class NodeEntityMapperImpl implements NodeEntityMapper {
    PositionMapper positionMapper;
    @Override
    public NodeEntity toEntity(Node node) {
        NodeEntity nodeEntity = new NodeEntity();

        // TODO: check if this is generated only through Neo4j
        nodeEntity.setGeneratedID(Long.parseLong(node.getId()));

        // TODO: check if Text is really the label name
        nodeEntity.setName(node.getText());

        // TODO: Description can be properties probably
        nodeEntity.setDescription(node.getText());

        // Set the type of the node
        nodeEntity.setType(node.getType());

        // Set the relations of the node
        nodeEntity.setRelations(node.getRelations().stream().map(relation -> toEntity(relation.getTarget())).collect(Collectors.toSet()));

        // Set the properties of the node
        // TODO: Not sure why we need an Object, should be probably a String (e.g Map<String, String>)
        nodeEntity.setProperties(node.getProperties().keySet());

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
        // TODO: Proably the name of the node
        node.setText(nodeEntity.getName());

        // TODO: Map relations back to the Node
        node.setRelations(nodeEntity.getRelations().stream().map(relation -> new Relation()).collect(Collectors.toSet()));

        // Set the type of the node
        node.setType(nodeEntity.getType());

        // TODO: Map properties back to the Node (should be a map ("color", "red"))
        node.setProperties(nodeEntity.getProperties().stream().collect(Collectors.toMap(property -> property, property -> "")));

        // TODO: Not sure what labels are
        node.setLabels(new HashSet<>());

        // Set the position of the node
        node.setUmletPosition(positionMapper.toLocation(nodeEntity.getPosition()));

        // Return the node
        return node;
    }
}
