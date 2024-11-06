package at.ac.tuwien.model.change.management.server.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ModelDTO(
        String id,
        @NotEmpty Set<NodeDTO> nodes
) {
}
