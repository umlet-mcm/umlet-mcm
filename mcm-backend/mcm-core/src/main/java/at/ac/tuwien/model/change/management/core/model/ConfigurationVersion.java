package at.ac.tuwien.model.change.management.core.model;

import javax.annotation.Nullable;

public record ConfigurationVersion(@Nullable String hash, @Nullable String name, @Nullable String customName) {
}
