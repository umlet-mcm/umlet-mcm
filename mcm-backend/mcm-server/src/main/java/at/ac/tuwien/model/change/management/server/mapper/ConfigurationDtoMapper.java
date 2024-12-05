package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfigurationDtoMapper {
    ConfigurationDTO toDto(Configuration configuration);
    Configuration fromDto(ConfigurationDTO dto);
    List<ConfigurationDTO> toDto(List<Configuration> configurations);
}
