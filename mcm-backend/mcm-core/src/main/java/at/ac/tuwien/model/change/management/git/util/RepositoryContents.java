package at.ac.tuwien.model.change.management.git.util;

import java.util.HashSet;

public record RepositoryContents<T>(
        HashSet<T> models,
        HashSet<T> nodes,
        HashSet<T> relations
) {
    public RepositoryContents() {
        this(new HashSet<>(), new HashSet<>(), new HashSet<>());
    }
}
