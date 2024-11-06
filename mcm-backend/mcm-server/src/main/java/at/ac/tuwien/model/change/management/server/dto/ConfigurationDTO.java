package at.ac.tuwien.model.change.management.server.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record ConfigurationDTO(
        @NotBlank String name,
        Set<ModelDTO> models
) {
}
