package at.ac.tuwien.model.change.management.git.util;

import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class PathUtils {
    private final static String SYSTEM_SEPARATOR = File.separator;
    private final static String UNIX_SEPARATOR = "/";
    private PathUtils() {}

    public static String normalizePath(@NonNull final String path) {
        return path.replace(SYSTEM_SEPARATOR, UNIX_SEPARATOR);
    }

    // Spring has a method for that, but it unfortunately doesn't set Files to writable
    // this causes problems on Windows
    public static boolean deleteFilesRecursively(@Nullable Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return false;
        }

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public @Nonnull FileVisitResult visitFile(Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                if (! Files.isWritable(file) && ! file.toFile().setWritable(true)) {
                    throw new IOException("Cannot delete " + file + " because it is not writable. " +
                            "Setting file to writable was attempted and failed.");
                }
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @Nonnull FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (! Files.isWritable(dir) && ! dir.toFile().setWritable(true)) {
                    throw new IOException("Cannot delete " + dir + " because it is not writable. " +
                            "Setting directory to writable was attempted and failed.");
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        return true;
    }
}
