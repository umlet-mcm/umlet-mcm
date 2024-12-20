package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.server.dto.RelationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {NodeDtoMapper.class})
public interface RelationDtoMapper {

    @Mapping(target = "target", source = "target.id")
    RelationDTO toDto(Relation relation);

    @Mapping(target = "target", ignore = true)
    Relation fromDto(RelationDTO dto);
}

