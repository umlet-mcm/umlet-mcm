package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Node;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NodeUpdaterImpl implements NodeUpdater {
    private final RelationUpdater relationUpdater;
    @Override
    public void updateNode(Node node, Node nodeToUpdate) {
        if(node == null || nodeToUpdate == null) {
            return;
        }

        // Set an assigned ID to the node
        nodeToUpdate.setId(node.getId());

        // Set the title of the node
        nodeToUpdate.setTitle(node.getTitle());

        // Set the description of the node
        nodeToUpdate.setDescription(node.getDescription());

        // Map relations back to the Node
        for (val relationToUpdate : nodeToUpdate.getRelations()) {
            // Find the corresponding model entity
            val relation = node.getRelations().stream()
                    .filter(relationItem -> {
                        assert relationItem.getId() != null;
                        return relationItem.getId().equals(relationToUpdate.getId());
                    })
                    .findFirst()
                    .orElse(null);
            relationUpdater.updateRelation(relation, relationToUpdate);
        }

        // Set the type of the node
        nodeToUpdate.setElementType(node.getElementType());

        // Set the PPR type of the node
        if(node.getPprType() != null) {
            nodeToUpdate.setPprType(node.getPprType());
        }

        // Maps the properties of the node entity back to node model
        nodeToUpdate.setMcmAttributes( node.getMcmAttributes() );
        nodeToUpdate.setUmletAttributes( node.getUmletAttributes());

        // Set the tags of the node
        nodeToUpdate.setTags( node.getTags());

        // Set the position of the node
        nodeToUpdate.setUmletPosition(node.getUmletPosition());
    }
}
