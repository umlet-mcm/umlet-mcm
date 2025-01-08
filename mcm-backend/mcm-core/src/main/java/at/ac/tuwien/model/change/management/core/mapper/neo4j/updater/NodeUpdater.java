package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Node;

public interface NodeUpdater {
    /**
     * Updates a Node with the values of a Node from DB
     *
     * @param node         the Node to update from
     * @param nodeToUpdate the Node to update
     */
    void updateNode(Node node, Node nodeToUpdate);
}
