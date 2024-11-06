package at.ac.tuwien.model.change.management.server.dto;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import jakarta.validation.constraints.NotNull;

public record RelationDTO(
        String type,
        @NotNull NodeDTO source,
        @NotNull NodeDTO target,
        @NotNull UMLetPosition umletPosition
) {
}
