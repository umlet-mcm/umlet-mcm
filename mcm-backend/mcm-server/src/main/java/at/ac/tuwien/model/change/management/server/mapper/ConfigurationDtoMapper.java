package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationValidationException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.RelationDTO;
import at.ac.tuwien.model.change.management.server.util.MappingUtils;
import lombok.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.*;

@Mapper(componentModel = "spring", uses = {ModelDtoMapper.class})
public interface ConfigurationDtoMapper {

    ConfigurationDTO toDto(Configuration configuration);

    Configuration fromDto(ConfigurationDTO dto);

    List<ConfigurationDTO> toDto(List<Configuration> configurations);

    @AfterMapping
    default void afterFromDto(@NonNull ConfigurationDTO dto, @MappingTarget @NonNull Configuration entity) {
        if (dto.models() == null) return;
        var nodesLookup = MappingUtils.createNodesLookupTable(entity);
        var relationsLookup = MappingUtils.createRelationsLookupTable(entity);

        dto.models().stream()
                .filter(Objects::nonNull)
                .flatMap(model -> Optional.ofNullable(model.nodes()).stream().flatMap(Collection::stream))
                .filter(Objects::nonNull)
                .flatMap(node -> Optional.ofNullable(node.relations()).stream().flatMap(Collection::stream))
                .filter(Objects::nonNull)
                .forEach(relationDTO -> setTargetNodeForRelation(relationDTO, nodesLookup, relationsLookup));
    }

    private void setTargetNodeForRelation(
            RelationDTO relationDTO,
            Map<String, Node> nodesLookupTable,
            Map<String, Relation> lookupTable
    ) {
        if (relationDTO.target() == null) return;
        var targetNode = Optional.ofNullable(nodesLookupTable.get(relationDTO.target()))
                .orElseThrow(() -> new ConfigurationValidationException(
                        "Target node with ID '" + relationDTO.target() + "' referenced by relation with ID '" + relationDTO.id() + "' not found"
                ));
        var relation = Optional.ofNullable(lookupTable.get(relationDTO.id()))
                .orElseThrow(() -> new IllegalStateException("Mapper failed to create relation for rekationDTO with ID: " + relationDTO.id()));
        relation.setTarget(targetNode);
    }
}

