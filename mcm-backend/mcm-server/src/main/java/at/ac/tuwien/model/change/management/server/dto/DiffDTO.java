package at.ac.tuwien.model.change.management.server.dto;

import lombok.NonNull;

public record DiffDTO(
        @NonNull String id,
        @NonNull String title,
        @NonNull String diffType,
        @NonNull String content){}
