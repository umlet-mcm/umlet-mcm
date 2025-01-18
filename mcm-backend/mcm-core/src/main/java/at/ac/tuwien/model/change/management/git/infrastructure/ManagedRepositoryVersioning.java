package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryVersioningException;
import at.ac.tuwien.model.change.management.git.util.PathUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ManagedRepositoryVersioning {
    private static final String ALL_FILES_PATTERN = ".";
    private static final String BRANCH_NAMESPACE = "refs/heads/";
    private static final String DEFAULT_BRANCH_NAME = "main";
    private static final String DEFAULT_BRANCH_REF = BRANCH_NAMESPACE + DEFAULT_BRANCH_NAME;

    private final Repository repository;
    private final String name;
    private final Charset encoding;
    private final Path workDir;

    /**
     * Initialize the repository.
     */
    public void init() {
        try {
            log.debug("Initializing repository: {}", name);
            try (var ignored = Git.init()
                    .setDirectory(workDir.toFile())
                    .setInitialBranch(DEFAULT_BRANCH_NAME)
                    .call()) {
                log.debug("Successfully initialized repository: {}", name);
            }
        } catch (GitAPIException e) {
            throw new RepositoryVersioningException("Failed to initialize repository: " + name, e);
        }
    }

    /**
     * Check if the repository is initialized.
     * technically the same as the {@link ManagedRepository#exists()} method which delegates to this
     *
     * @return true if the repository is initialized, false otherwise
     */
    public boolean isInitialized() {
        return repository.getObjectDatabase() != null && repository.getObjectDatabase().exists();
    }

    /**
     * Stage files in the repository.
     * @param files the files to stage
     * @return the number of files staged
     */
    public int stageFiles(@NonNull Collection<Path> files) {
        if (! isInitialized()) {
            throw new RepositoryVersioningException("Cannot stage files in uninitialized repository: " + name);
        }

        log.debug("Staging {} files in repository: {}", files.size(), name);
        if (files.isEmpty()) {
            log.debug("No files to stage");
            return 0;
        }

        var dirCache = applyStagingAction(add -> files.forEach(file -> {
            if (!workDir.resolve(file).normalize().startsWith(workDir)) {
                throw new RepositoryVersioningException("Attempted to stage file '"
                        + file + "' outside repository: " + name);
            }
            add.addFilepattern(PathUtils.normalizePath(workDir.relativize(file).toString()));
        }));

        var entryCount = dirCache.getEntryCount();
        log.debug("Staged {} files in repository: {}", entryCount, name);
        return entryCount;
    }

    /**
     * Stage all files in the repository.
     * @return the number of files staged
     */
    public int stageAll() {
        if (! isInitialized()) {
            throw new RepositoryVersioningException("Cannot stage all files in uninitialized repository: " + name);
        }

        log.debug("Staging all files in working directory of repository: {}", name);
        var dirCache = applyStagingAction(add -> add.addFilepattern(ALL_FILES_PATTERN));
        var entryCount = dirCache.getEntryCount();
        log.debug("Staged all {} files in working directory of repository: {}", entryCount, name);
        return entryCount;
    }

    /**
     * Commit staged changes in the repository.
     * @param message the commit message
     * @param stageModifiedFiles whether to automatically stage modified files before committing
     *                           this will include files that have been part of the previous commit
     *                           but not untracked files in the working directory - those have to be staged
     *                           explicitly via {@link #stageFiles(Collection)} or {@link #stageAll()}
     * @return the hash of the created commit
     */
    public String commit(@NonNull String message, boolean stageModifiedFiles) {
        if (! isInitialized()) {
            throw new RepositoryVersioningException("Cannot commit changes in uninitialized repository: " + name);
        }

        log.debug("Committing changes with message '{}' in repository: {}", message, name);
        if (message.isBlank()) {
            throw new IllegalArgumentException("Commit message cannot be empty");
        }

        try (var git = Git.wrap(repository)) {
            var commit = git.commit()
                    .setMessage(message)
                    .setAll(stageModifiedFiles)
                    .call();
            var commitHash = commit.getName();
            log.debug("Created commit '{}' in repository: {}", commitHash, name);
            return commitHash;
        } catch (GitAPIException e) {
            throw new RepositoryVersioningException("Failed to commit changes in repository: " + name, e);
        }
    }

    /**
     * Get the current version ID (commit hash) associated with the repository HEAD.
     * @return the current version ID, or an empty Optional if the repository is not initialized
     * or the HEAD commit cannot be resolved for other reasons
     */
    public Optional<String> getCurrentVersionId() {
        if (! isInitialized()) {
            log.debug("Cannot get current version ID of uninitialized repository: {}", name);
            return Optional.empty();
        }

        log.debug("Getting current version ID of repository: {}", name);
        return resolveHead().map(ObjectId::getName);
    }

    /**
     * List all versions on the main branch of the repository.
     * @return a list of version IDs (commit hashes) on the main branch, or an empty list if no such versions exist
     */
    public List<String> listVersions() {
        return listVersions(DEFAULT_BRANCH_REF, false);
    }

    /**
     * List all versions on the main branch of the repository.
     * @param ascending whether to list versions in ascending order
     *                  - they are listed in descending order when {@link #listVersions()} is called
     * @return a list of version IDs (commit hashes) on the main branch, or an empty list if no such versions exist
     */
    public List<String> listVersions(boolean ascending) {
        return listVersions(DEFAULT_BRANCH_REF, ascending);
    }

    /**
     * List all versions starting from a specific version identifier
     * that could be a branch name, a tag, a commit hash, etc.
     * @param start the version identifier to start listing versions from
     * @param ascending whether to list versions in ascending order
     *                  - they are listed in descending order when {@link #listVersions()} is called
     * @return a list of version IDs (commit hashes) starting from the specified version, or an empty list if no such versions exist
     */
    public List<String> listVersions(@NonNull String start, boolean ascending) {
        if (! isInitialized()) {
            log.debug("Cannot list versions in uninitialized repository: {}", name);
            return Collections.emptyList();
        }

        log.debug("Listing versions in repository: {}", name);
        try (var git = Git.wrap(repository)) {
            return resolve(start).map(headCommit -> {
                try {
                    List<String> versions = new ArrayList<>();
                    for (var ref : git.log().add(headCommit).call()) {
                        versions.add(ref.getName());
                    }
                    return ascending ? versions.reversed() : versions;
                } catch (IOException | GitAPIException e) {
                    throw new RepositoryVersioningException("Failed to list versions in repository: " + name, e);
                }
            }).orElseGet(() -> {
                log.debug("No commits found in repository '{}' while listing versions", name);
                return Collections.emptyList();
            });
        }
    }

    /**
     * Compare two versions of the repository.
     * @param oldVersion the old version to compare with
     * @param newVersion the new version to compare with
     * @param includeUnchanged whether to include unchanged objects in the comparison results
     *                         if set to true, these will be included as "UNCHANGED" diff entries with the content simply
     *                         being the object file's String representation (i.e., not including any Git headers or hunks)
     * @param objectPreprocessor a function to preprocess the object content before comparison
     *                           can be used to, e.g., remove metadata that we don't want to include in our diffs
     * @return a list of differences between the two versions of the repository as {@link ManagedDiffEntry} objects
     */
    public List<ManagedDiffEntry> compareVersions(
            @NonNull String oldVersion,
            @NonNull String newVersion,
            boolean includeUnchanged,
            @Nullable Function<ManagedRepositoryObject, byte[]> objectPreprocessor
    ) {
        if (! isInitialized()) {
            throw new RepositoryVersioningException("Cannot compare versions in uninitialized repository: " + name);
        }

        try (var diffFmt = new ManagedDiffFormatter(repository, encoding, name)) {
            var oldCommit = resolveCommit(oldVersion)
                    .orElseThrow(() -> new RepositoryVersioningException("Old version '" + oldVersion + "' not found in repository: " + name));
            var newCommit = resolveCommit(newVersion)
                    .orElseThrow(() -> new RepositoryVersioningException("New version '" + newVersion + "' not found in repository: " + name));
            return diffFmt.createUnifiedDiff(oldCommit, newCommit, includeUnchanged, objectPreprocessor);
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to compare old version '" + oldVersion +
                    "' with new version + '" + newVersion + "' in repository: " + name, e);
        }
    }

    /**
     * Checkout a specific version of the repository.
     * @param version the version to `git checkout`
     */
    public void checkout(@NonNull String version) {
        if (! isInitialized()) {
            throw new RepositoryVersioningException("Cannot checkout version in uninitialized repository: " + name);
        }

        log.debug("Checking out version '{}' in repository: {}", version, name);
        try (var git = Git.wrap(repository)) {
            // setting forced=true, because any files in the working tree should immediately be committed after being written
            // so we should never be in a position where we are discarding changes
            git.checkout().setForced(true).setName(version).call();
            log.debug("Checked out version '{}' in repository: {}", version, name);
        } catch (GitAPIException e) {
            throw new RepositoryVersioningException("Failed to checkout version '" + version + "' in repository: " + name, e);
        }
    }

    /**
     * Reset the repository to a specific version.
     * @param version the version to reset to
     */
    public void reset(@NonNull String version) {
        if (! isInitialized()) {
            throw new RepositoryVersioningException("Cannot reset to version in uninitialized repository: " + name);
        }

        try {
            log.debug("Resetting to version '{}' in repository: {}", version, name);
            new ResetCommand(repository).setMode(ResetCommand.ResetType.HARD).setRef(version).call();
            log.debug("Reset to version '{}' in repository: {}", version, name);
        } catch (GitAPIException | JGitInternalException e) {
            // for some reason the reset command can throw an internal runtime exception, e.g., when the ref is invalid
            // we prefer to wrap this in our own exception though. So catching that here as well
            throw new RepositoryVersioningException("Failed to reset to version '" + version + "' in repository: " + name, e);
        }
    }

    private DirCache applyStagingAction(Consumer<AddCommand> action) {
        try {
            var addCommand = new AddCommand(repository);
            action.accept(addCommand);
            return addCommand.call();
        } catch (GitAPIException e) {
            throw new RepositoryVersioningException("Failed to perform staging in repository: " + name, e);
        }
    }

    private Optional<ObjectId> resolveHead() {
        return resolve(Constants.HEAD);
    }

    private Optional<RevCommit> resolveCommit(String id) {
            return resolve(id).map(objectId -> {
                try {
                    return repository.parseCommit(objectId);
                } catch (IOException e) {
                    throw new RepositoryVersioningException("Failed to parse commit: " + id, e);
                }
            });
    }

    private Optional<ObjectId> resolve(String id) {
        try {
            return Optional.ofNullable(repository.resolve(id));
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to resolve current version of repository: " + name, e);
        }
    }
}
