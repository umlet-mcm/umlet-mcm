package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import org.mapstruct.Mapper;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;

@Mapper(componentModel = "spring")
public interface NodeEntityMapper {
    NodeEntity toEntity(Node nodeEntity);
    Node fromEntity(NodeEntity nodeEntity);
}
