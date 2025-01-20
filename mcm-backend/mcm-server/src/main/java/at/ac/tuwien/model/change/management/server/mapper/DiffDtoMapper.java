package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.versioning.BaseAttributesDiff;
import at.ac.tuwien.model.change.management.server.dto.DiffDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DiffDtoMapper {

    @Mapping(source = "element.id", target = "id")
    @Mapping(source = "element.title", target = "title")
    DiffDTO toDto(BaseAttributesDiff model);

    List<DiffDTO> toDto(List<BaseAttributesDiff> model);
}
