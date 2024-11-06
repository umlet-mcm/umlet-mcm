package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NodeDtoMapper {
    NodeDTO toDto(Node node);
    Node fromDto(NodeDTO dto);
}
