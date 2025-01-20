package at.ac.tuwien.model.change.management.git.infrastructure;

import lombok.NonNull;

import java.util.Collections;
import java.util.List;

public record ManagedRepositoryVersion(
        @NonNull String id,
        @NonNull List<String> tags,
        @NonNull List<ManagedRepositoryObject> objects
) {

    @Override
    public List<ManagedRepositoryObject> objects() {
        return Collections.unmodifiableList(objects);
    }
}

