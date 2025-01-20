package at.ac.tuwien.model.change.management.git.infrastructure;

import java.util.Collections;
import java.util.List;

public record ManagedRepositoryVersion(String id, List<ManagedRepositoryObject> objects) {

    @Override
    public List<ManagedRepositoryObject> objects() {
        return Collections.unmodifiableList(objects);
    }
}

