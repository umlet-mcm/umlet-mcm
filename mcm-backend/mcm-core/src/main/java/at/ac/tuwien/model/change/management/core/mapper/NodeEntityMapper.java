package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;

/**
 * Mapper for converting between Node and NodeEntity
 */
//@Mapper(componentModel = "spring")
public interface NodeEntityMapper {
    /**
     * Converts a Node to a NodeEntity
     *
     * @param node the Node to convert
     * @return the converted NodeEntity
     */
    NodeEntity toEntity(Node node);

    /**
     * Converts a NodeEntity to a Node
     *
     * @param nodeEntity the NodeEntity to convert
     * @return the converted Node
     */
    Node fromEntity(NodeEntity nodeEntity);
}
