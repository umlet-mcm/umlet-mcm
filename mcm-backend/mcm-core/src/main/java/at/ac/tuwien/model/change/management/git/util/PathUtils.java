package at.ac.tuwien.model.change.management.git.util;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
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

    private PathUtils() {
    }

    public static String normalizePath(@NonNull final String path) {
        return path.replace(SYSTEM_SEPARATOR, UNIX_SEPARATOR);
    }

    public static boolean renameFile(@NonNull Path source, @NonNull Path target) throws IOException {
        if (Files.exists(target)) {
            throw new RepositoryAlreadyExistsException("Cannot rename " + source + " to " + target + " because target already exists.");
        }
        return source.toFile().renameTo(target.toFile());
    }

    public static boolean deleteFilesRecursively(@Nullable Path root) throws IOException {
        return deleteFilesRecursively(root, false);
    }

    // Spring has a method for that, but it unfortunately doesn't set Files to writable
    // this causes problems on Windows
    public static boolean deleteFilesRecursively(@Nullable Path root, boolean keepRootDirectory) throws IOException {
        if (root == null || !Files.exists(root)) {
            return false;
        }

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public @Nonnull FileVisitResult visitFile(Path file, @Nonnull BasicFileAttributes attrs) throws IOException {
                if (!Files.isWritable(file) && !file.toFile().setWritable(true)) {
                    throw new IOException("Cannot delete " + file + " because it is not writable. " +
                            "Setting file to writable was attempted and failed.");
                }
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @Nonnull FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (keepRootDirectory && dir.equals(root)) {
                    return FileVisitResult.CONTINUE;
                }

                if (!Files.isWritable(dir) && !dir.toFile().setWritable(true)) {
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
