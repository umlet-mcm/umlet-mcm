package at.ac.tuwien.model.change.management.server.dto;

import lombok.NonNull;
import org.springframework.lang.Nullable;

public record DiffDTO(
        @NonNull String id,
        @Nullable String title,
        @NonNull String diffType,
        @NonNull String content){}
