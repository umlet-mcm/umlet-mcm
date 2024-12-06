package at.ac.tuwien.model.change.management.core.utils;

import lombok.NonNull;

import java.io.File;
import java.util.regex.Pattern;

public final class PathUtils {
    private final static String SYSTEM_SEPARATOR = Pattern.quote(File.separator);
    private final static String UNIX_SEPARATOR = "/";
    private PathUtils() {}

    public static String normalizePath(@NonNull final String path) {
        return path.replaceAll(SYSTEM_SEPARATOR, UNIX_SEPARATOR);
    }
}
