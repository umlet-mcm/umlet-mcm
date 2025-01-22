package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.ConfigurationVersion;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationVersionDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConfigurationVersionDtoMapper {
    List<ConfigurationVersionDTO> toDto(List<ConfigurationVersion> configurationVersions);
}
