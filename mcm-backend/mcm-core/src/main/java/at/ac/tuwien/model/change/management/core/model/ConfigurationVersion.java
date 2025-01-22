package at.ac.tuwien.model.change.management.core.model;

import org.springframework.lang.Nullable;


public record ConfigurationVersion(@Nullable String hash, @Nullable String name, @Nullable String customName) {

    @SuppressWarnings("unused")
    public ConfigurationVersion withHash(@Nullable String hash) {
        return new ConfigurationVersion(hash, name, customName);
    }

    @SuppressWarnings("unused")
    public ConfigurationVersion withName(@Nullable String name) {
        return new ConfigurationVersion(hash, name, customName);
    }

    public ConfigurationVersion withCustomName(@Nullable String customName) {
        return new ConfigurationVersion(hash, name, customName);
    }
}
