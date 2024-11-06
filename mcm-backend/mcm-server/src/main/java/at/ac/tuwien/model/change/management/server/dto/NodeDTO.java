package at.ac.tuwien.model.change.management.server.dto;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;
import java.util.Set;

public record NodeDTO(
        String id,
        @NotBlank String text,
        Set<RelationDTO> relations,
        String type,
        Map<String, Object> properties,
        Set<String> labels,
        UMLetPosition umletPosition
) {
}
