package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NodeEntityMapper {
    NodeEntity toEntity(Node nodeEntity);

    Node fromEntity(NodeEntity nodeEntity);
}
