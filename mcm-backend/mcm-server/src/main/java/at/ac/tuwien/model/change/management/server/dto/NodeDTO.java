package at.ac.tuwien.model.change.management.server.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public record NodeDTO(
        String elementType,
        List<Integer> generatedAttributes,
        UMLetPositionDTO umletPosition,
        Set<RelationDTO> relations,
        String id,
        List<String> tags,
        String originalText,
        String title,
        String description,
        LinkedHashMap<String, Object> mcmAttributes,
        String mcmModel,
        LinkedHashMap<String, String> umletAttributes,
        String pprType
) {
}
