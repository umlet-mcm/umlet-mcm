package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.*;
import at.ac.tuwien.model.change.management.git.util.PathUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@AllArgsConstructor
public class ManagedRepository implements AutoCloseable {
    private final Repository repository;
    @Getter
    private String name;
    @Getter
    private final Charset encoding;
    @Getter
    @Accessors(fluent = true)
    private final ManagedRepositoryVersioning versioning;
    private final Path workDir;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Check if the repository exists.
     *
     * @return true if the repository exists, false otherwise
     */
    public boolean exists() {
        return versioning.isInitialized();
    }

    /**
     * Write files to the repository.
     * The file path is concatenated to the working directory of the repository.
     *
     * @param files the files to write, including both their path and String content
     * @return a list of the written files as Path objects
     */
    public List<Path> writeRepositoryFiles(@NonNull Collection<ManagedRepositoryFile> files) {
        log.debug("Writing {} files to repository: {}", files.size(), getName());
        return files.stream().map(this::writeToWorkingDirectory).toList();
    }

    /**
     * Delete files or directories from the repository.
     * Recursively deletes the file or directory at the given path and - if it is a directory - all its contents.
     *
     * @param files the files or directories to delete
     */
    public void deleteRepositoryFiles(@NonNull Path... files) {
        log.debug("Deleting files from repository '{}': {}", getName(), files);
        for (Path file : files) {
            try {
                var finalPath = resolvePath(file);
                PathUtils.deleteFilesRecursively(finalPath);
                log.debug("Deleted '{}' from repository '{}'", file, getName());
            } catch (IOException e) {
                throw new RepositoryDeleteException("Cannot delete repository path: " + file, e);
            }
        }
    }

    /**
     * Get the current version of the repository.
     *
     * @return the current version of the repository, or an empty Optional if the repository does not exist
     */
    public Optional<ManagedRepositoryVersion> getCurrentRepositoryVersion() {
        if (!exists()) {
            log.debug("Cannot get current version of repository '{}', because it does not exist", getName());
            return Optional.empty();
        }

        return getRepositoryVersion(Constants.HEAD);
    }

    /**
     * Get a specific version of the repository.
     *
     * @param version the version to get
     * @return the version of the repository, or an empty Optional if the repository does not exist
     */
    public Optional<ManagedRepositoryVersion> getRepositoryVersion(@NonNull String version) {
        if (!exists()) {
            log.debug("Cannot get version '{}' of repository '{}', because it does not exist", version, getName());
            return Optional.empty();
        }

        try {
            log.debug("Finding objects for version '{}' from repository: {}", version, getName());
            var objectId = repository.resolve(version);
            if (objectId == null) {
                log.debug("Failed to resolve version '{}' of repository '{}'", version, getName());
                return Optional.empty();
            }
            var commit = repository.parseCommit(objectId);
            var commitHash = commit.getName();
            var commitTags = versioning().listTagsForCommit(commitHash);

            try (var treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                var objects = new ArrayList<ManagedRepositoryObject>();

                while (treeWalk.next()) {
                    objects.add(new ManagedRepositoryObject(treeWalk, repository, encoding));
                }

                log.debug("Found {} objects for version '{}' in repository: {}", objects.size(), commitHash, getName());
                return Optional.of(new ManagedRepositoryVersion(commitHash, commitTags, objects));
            }
        } catch (IOException e) {
            throw new RepositoryReadException("Failed to read repository version: " + version, e);
        }
    }

    /**
     * Delete the repository.
     */
    public void deleteRepository() {
        try {
            PathUtils.deleteFilesRecursively(workDir);
        } catch (IOException e) {
            throw new RepositoryDeleteException("Failed to delete repository: " + name, e);
        }
    }

    /**
     * Rename the repository.
     */
    public void renameRepository(String newName) {
        try {
            PathUtils.renameFile(workDir, workDir.resolveSibling(newName));
            name = newName;
            log.debug("Renamed repository: {} to: {}", name, newName);
        } catch (IOException e) {
            throw new RepositoryRenameException("Failed to rename repository: " + name + " to: " + newName, e);
        }
    }

    /**
     * Close the repository.
     * This method should be called when the repository is no longer needed.
     * Underlying resources such as the JGit repository SHOULD be closed, so don't forget to call this method.
     */
    @Override
    public void close() {
        log.debug("Attempting to close repository: {}", getName());
        if (closed.compareAndSet(false, true)) {
            repository.close();
            log.debug("Closed repository: {}", getName());
        }
    }

    private Path writeToWorkingDirectory(@NonNull ManagedRepositoryFile file) {
        try {
            var finalPath = resolvePath(file.path());
            Files.createDirectories(finalPath.getParent());
            var writtenFile = Files.writeString(finalPath, file.content(), encoding);
            log.debug("Wrote file '{}' to repository '{}'", writtenFile, getName());
            return writtenFile;
        } catch (IOException e) {
            throw new RepositoryWriteException("Failed to write file to working directory: " + file.path(), e);
        }
    }

    private Path resolvePath(Path path) {
        Path resolved = workDir.resolve(path).normalize();
        if (!resolved.startsWith(workDir)) {
            throw new RepositoryAccessException("Tried to access path '" + path + "' outside repository: " + name);
        }
        return resolved;
    }
}

