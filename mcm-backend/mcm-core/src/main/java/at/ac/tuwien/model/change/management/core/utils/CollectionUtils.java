package at.ac.tuwien.model.change.management.core.utils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public final class CollectionUtils {

    private CollectionUtils() {}

    public static <T> Set<T> tryAccessSet(@Nullable Set<T> set) {
        if (set == null) {
            return Collections.emptySet();
        }
        return set;
    }
}
