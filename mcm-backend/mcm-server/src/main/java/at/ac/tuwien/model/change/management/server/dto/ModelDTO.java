package at.ac.tuwien.model.change.management.server.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public record ModelDTO(
        @NotEmpty Set<NodeDTO> nodes,
        String id,
        List<String> tags,
        String title,
        String description,
        LinkedHashMap<String, Object> mcmAttributes,
        LinkedHashMap<String, String> mcmAttributesInlineComments,
        int zoomLevel
) {
}