package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {NodeDtoMapper.class, RelationDtoMapper.class})
public interface ModelDtoMapper {
    
    ModelDTO toDto(Model model);

    Model fromDto(ModelDTO dto);
}
