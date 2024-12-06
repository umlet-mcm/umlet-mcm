package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RelationDtoMapper.class})
public interface NodeDtoMapper {

    NodeDTO toDto(Node node, @Context CycleAvoidingMappingContext context);

    Node fromDto(NodeDTO dto, @Context CycleAvoidingMappingContext context);
}

