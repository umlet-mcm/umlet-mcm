package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.server.dto.RelationDTO;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RelationDtoMapper {

    RelationDTO toDto(Relation relation, @Context CycleAvoidingMappingContext context);

    Relation fromDto(RelationDTO dto, @Context CycleAvoidingMappingContext context);
}

