package at.ac.tuwien.model.change.management.server.dto;

public record ConfigurationVersionDTO(
        String hash,
        String name,
        String customName
) {
}
