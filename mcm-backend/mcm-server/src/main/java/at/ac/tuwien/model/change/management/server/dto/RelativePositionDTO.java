package at.ac.tuwien.model.change.management.server.dto;

public record RelativePositionDTO(
        int absX,
        int absY,
        int offsetX,
        int offsetY
) {
}