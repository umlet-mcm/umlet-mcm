package at.ac.tuwien.model.change.management.server.dto;

import java.util.LinkedHashMap;
import java.util.List;

public record RelationDTO(
        String type,
        String target,
        UMLetPositionDTO umletPosition,
        RelativePositionDTO relativeStartPoint,
        List<RelativePositionDTO> relativeMidPoints,
        RelativePositionDTO relativeEndPoint,
        PointDTO startPoint,
        PointDTO endPoint,
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