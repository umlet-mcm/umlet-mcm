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
        String title,
        String description,
        LinkedHashMap<String, Object> mcmAttributes,
        LinkedHashMap<String, String> mcmAttributesInlineComments,
        String mcmModel,
        String mcmModelId,
        LinkedHashMap<String, String> umletAttributes,
        String pprType
) {
}
