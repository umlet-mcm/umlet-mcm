package at.ac.tuwien.model.change.management.server.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

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
        String originalText,
        String title,
        String description,
        LinkedHashMap<String, Object> mcmAttributes,
        String mcmModel,
        String mcmModelId,
        LinkedHashMap<String, String> umletAttributes,
        String pprType
) {


    // We have to override the default Record implementation of equals and hashcode
    // because otherwise we have a circular dependency between NodeDTO and RelationDTO
    // resulting in a StackOverflowError

    // Since the other fields implement a detailed position (expected to be unique)
    // and the id assigned by the data layer, excepting the target node should be safe
    @Override
    public boolean equals(Object o) {
       if (this == o) return true;
       if (! (o instanceof RelationDTO that)) return false;

       return Objects.equals(type(), that.type()) &&
              Objects.equals(umletPosition(), that.umletPosition()) &&
              Objects.equals(relativeStartPoint(), that.relativeStartPoint()) &&
              Objects.equals(relativeMidPoints(), that.relativeMidPoints()) &&
              Objects.equals(relativeEndPoint(), that.relativeEndPoint()) &&
              Objects.equals(startPoint(), that.startPoint()) &&
              Objects.equals(endPoint(), that.endPoint()) &&
              Objects.equals(id(), that.id()) &&
              Objects.equals(tags(), that.tags()) &&
              Objects.equals(originalText(), that.originalText()) &&
              Objects.equals(title(), that.title()) &&
              Objects.equals(description(), that.description()) &&
              Objects.equals(mcmAttributes(), that.mcmAttributes()) &&
              Objects.equals(mcmModel(), that.mcmModel()) &&
              Objects.equals(mcmModelId(), that.mcmModelId()) &&
              Objects.equals(umletAttributes(), that.umletAttributes()) &&
              Objects.equals(pprType(), that.pprType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type(),
                umletPosition(),
                relativeStartPoint(),
                relativeMidPoints(),
                relativeEndPoint(),
                startPoint(),
                endPoint(),
                id(),
                tags(),
                originalText(),
                title(),
                description(),
                mcmAttributes(),
                mcmModel(),
                mcmModelId(),
                umletAttributes(),
                pprType());
    }
}
