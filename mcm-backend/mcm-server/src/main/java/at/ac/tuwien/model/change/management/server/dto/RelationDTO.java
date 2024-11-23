package at.ac.tuwien.model.change.management.server.dto;

import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import jakarta.validation.constraints.NotNull;

public record RelationDTO(
        String type,
        @NotNull NodeDTO source,
        @NotNull NodeDTO target,
        @NotNull UmletPositionUxf umletPosition
) {
}
