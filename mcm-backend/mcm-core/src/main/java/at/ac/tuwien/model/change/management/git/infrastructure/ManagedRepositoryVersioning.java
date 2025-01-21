package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryVersioningException;
import at.ac.tuwien.model.change.management.git.util.PathUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.hooks.Hooks;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
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
    private static final char DEFAULT_GIT_COMMENT_CHAR = '#';

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
     *
     * @param files the files to stage
     * @return the number of files staged
     */
    public int stageFiles(@NonNull Collection<Path> files) {
        if (!isInitialized()) {
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
     *
     * @return the number of files staged
     */
    public int stageAll() {
        if (!isInitialized()) {
            throw new RepositoryVersioningException("Cannot stage all files in uninitialized repository: " + name);
        }

        log.debug("Staging all files in working directory of repository: {}", name);
        var dirCache = stageAll(false);
        var entryCount = dirCache.getEntryCount();
        log.debug("Staged all {} files in working directory of repository: {}", entryCount, name);
        return entryCount;
    }

    /**
     * Commit staged changes ON THE MAIN BRANCH of the repository.
     * I.e., if HEAD and main differ, it will update the main branch, not HEAD as in most Git implementations
     *
     * @param message            the commit message
     * @param stageModifiedFiles whether to automatically stage modified files before committing
     * @return the hash of the created commit
     */
    public String commit(@NonNull String message, boolean stageModifiedFiles) {
        return commit(DEFAULT_BRANCH_REF, message, stageModifiedFiles);
    }

    /**
     * Commit staged changes in the repository on a specific branch or reference.
     * Note that the reference can be HEAD
     * if HEAD and the reference match, the HEAD will be updated as well
     * if HEAD and the reference differ, only the reference will be updated
     *
     * @param referenceToUpdate  the reference to update with the new commit
     *                           this can be a branch name, a tag, a commit hash, etc.
     *                           the commit the reference points to will be the parent of the new commit
     * @param message            the commit message
     * @param stageModifiedFiles whether to automatically stage modified files before committing
     *                           this will include files that have been part of the previous commit
     *                           but not untracked files in the working directory - those have to be staged
     *                           explicitly via {@link #stageFiles(Collection)} or {@link #stageAll()}
     * @return the hash of the created commit
     */
    public String commit(@NonNull String referenceToUpdate, @NonNull String message, boolean stageModifiedFiles) {
        if (!isInitialized()) {
            throw new RepositoryVersioningException("Cannot commit changes in uninitialized repository: " + name);
        }

        // we want to update the HEAD, if HEAD and reference match
        // because in that case we want to move both HEAD and reference
        var headCommitId = resolveHead().orElse(ObjectId.zeroId());
        var updateReferenceID = resolve(referenceToUpdate).orElse(ObjectId.zeroId());
        var updateMatchesHead = updateReferenceID.getName().equals(headCommitId.getName());
        var updateReference = updateMatchesHead ? Constants.HEAD : referenceToUpdate;
        var parentCommit = updateMatchesHead
                ? (headCommitId.equals(ObjectId.zeroId()) ? null : headCommitId)
                : updateReferenceID;

        log.debug("Committing changes with message '{}' in repository: {}", message, name);
        if (message.isBlank()) {
            throw new IllegalArgumentException("Commit message cannot be empty");
        }

        var commit = buildCommit(message, stageModifiedFiles, false, updateReference, parentCommit);
        var commitHash = commit.getName();
        log.debug("Created commit '{}' in repository: {}", commitHash, name);
        return commitHash;
    }

    /**
     * Get the current version ID (commit hash) associated with the repository HEAD.
     *
     * @return the current version ID, or an empty Optional if the repository is not initialized
     * or the HEAD commit cannot be resolved for other reasons
     */
    public Optional<String> getCurrentVersionId() {
        if (!isInitialized()) {
            log.debug("Cannot get current version ID of uninitialized repository: {}", name);
            return Optional.empty();
        }

        log.debug("Getting current version ID of repository: {}", name);
        return resolveHead().map(ObjectId::getName);
    }

    /**
     * List all versions on the main branch of the repository.
     *
     * @return a list of version IDs (commit hashes) on the main branch, or an empty list if no such versions exist
     */
    public List<String> listVersions() {
        return listVersions(DEFAULT_BRANCH_REF, false);
    }

    /**
     * List all versions on the main branch of the repository.
     *
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
     *
     * @param start     the version identifier to start listing versions from
     * @param ascending whether to list versions in ascending order
     *                  - they are listed in descending order when {@link #listVersions()} is called
     * @return a list of version IDs (commit hashes) starting from the specified version, or an empty list if no such versions exist
     */
    public List<String> listVersions(@NonNull String start, boolean ascending) {
        if (!isInitialized()) {
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
     *
     * @param oldVersion         the old version to compare with
     * @param newVersion         the new version to compare with
     * @param includeUnchanged   whether to include unchanged objects in the comparison results
     *                           if set to true, these will be included as "UNCHANGED" diff entries with the content simply
     *                           being the object file's String representation (i.e., not including any Git headers or hunks)
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
        if (!isInitialized()) {
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
     *
     * @param version the version to `git checkout`
     */
    public void checkout(@NonNull String version) {
        if (!isInitialized()) {
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
     *
     * @param version the version to reset to
     */
    public void reset(@NonNull String version) {
        if (!isInitialized()) {
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

    /**
     * Tag the specified commit in the repository.
     */
    public void tagCommit(@NonNull String commit, @NonNull String tagName) {
        if (!isInitialized()) {
            throw new RepositoryVersioningException("Cannot tag commit in uninitialized repository: " + name);
        }

        try (var git = Git.wrap(repository)) {
            log.debug("Tagging commit '{}' with tag '{}' in repository: {}", commit, tagName, name);
            git.tag().setObjectId(resolveCommit(commit)
                    .orElseThrow(() -> new RepositoryVersioningException("Commit '" + commit + "' not found in repository: " + name)))
                    .setName(tagName)
                    .call();
            log.debug("Tagged commit '{}' with tag '{}' in repository: {}", commit, tagName, name);
        } catch (GitAPIException e) {
            throw new RepositoryVersioningException("Failed to tag commit '" + commit + "' with tag '" + tagName + "' in repository: " + name, e);
        }
    }

    /**
     * List all tags in the repository.
     * @return a list of tag names in the repository
     */
    public List<String> listTags(){
        log.debug("Listing tags in repository: {}", name);
        if (!isInitialized()) {
            throw new RepositoryVersioningException("Cannot list tags in uninitialized repository: " + name);
        }
        var tags = listTags(null);
        log.debug("Listed {} tags in repository: {}", tags.size(), name);
        return tags;
    }

    /**
     * List all tags for a specific commit in the repository.
     * @param forCommit the commit to list tags for
     * @return a list of tag names for the specified commit in the repository
     */
    public List<String> listTagsForCommit(@NonNull String forCommit) {
        log.debug("Listing tags for commit '{}' in repository: {}", forCommit, name);
        if (!isInitialized()) {
            throw new RepositoryVersioningException("Cannot list tags in uninitialized repository: " + name);
        }
        var commit = resolveCommit(forCommit)
                .orElseThrow(() -> new RepositoryVersioningException("Commit '" + forCommit + "' not found in repository " +
                        "when trying to list its tags: " + name));
        var tags = listTags(commit);
        log.debug("Listed {} tags for commit '{}' in repository: {}", tags.size(), forCommit, name);
        return tags;
    }

    private List<String> listTags(@Nullable ObjectId forCommit) {
        try (var git = Git.wrap(repository)) {
            var listTagsCommand = git.tagList();
            if (forCommit != null) {
                listTagsCommand.setContains(forCommit);
            }
            return listTagsCommand.call().stream()
                    .map(Ref::getName)
                    .map(this::stripTags)
                    .toList();
        } catch (GitAPIException e) {
            throw new RepositoryVersioningException("Failed to list tags in repository: " + name, e);
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to list tags in repository '" + name +
                    "' for commit '" + forCommit.getName() + "'", e);
        }
    }

    private String stripTags(String tagName) {
        return tagName.replace(Constants.R_TAGS, "");
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

    /*
     * Build a commit in the repository.
     * This method is largely a copy of the JGit {@link org.eclipse.jgit.api.CommitCommand} implementation
     * However, it allows setting the commit parent (which is the reason it was created)
     * Note that a bunch of functionality is missing relative to the original implementation
     * This includes
     * - commit hooks
     * - merges
     * - signing
     * possibly more
     */
    @SuppressWarnings("SameParameterValue")
    private RevCommit buildCommit(
            String message,
            boolean stageModifiedFiles,
            boolean noVerify,
            String referenceToUpdate,
            @Nullable ObjectId parent
    ) {
        try (RevWalk rw = new RevWalk(repository)) {
            RepositoryState state = repository.getRepositoryState();

            if (!state.canCommit()) {
                throw new RepositoryVersioningException("Cannot commit in repository '" + name + "' in state: " + state.name());
            }

            if (!noVerify) {
                Hooks.preCommit(repository, null, null).call();
            }

            if (stageModifiedFiles) {
                stageAll(true);
            }

            if (!noVerify) {
                message = Hooks.commitMsg(repository, null, null)
                        .setCommitMessage(message)
                        .call();
            }

            message = CommitConfig.cleanText(message, CommitConfig.CleanupMode.WHITESPACE, DEFAULT_GIT_COMMENT_CHAR);

            DirCache index = repository.lockDirCache();
            RevCommit revCommit;
            try (ObjectInserter odi = repository.newObjectInserter()) {
                ObjectId indexTreeId = index.writeTree(odi);
                var commitBuilder = configureCommitBuilder(message, indexTreeId, parent);
                ObjectId commitId = odi.insert(commitBuilder);
                odi.flush();
                revCommit = rw.parseCommit(commitId);
                updateRef(referenceToUpdate, revCommit, commitId, parent);
            } finally {
                index.unlock();
            }

            try {
                Hooks.postCommit(repository, null, null).call();
            } catch (Exception e) {
                log.error("Post-commit hook failed in repository: {}", name, e);
            }

            return revCommit;
        } catch (AbortedByHookException e) {
            throw new RepositoryVersioningException("Commit aborted by hook in repository: " + name, e);
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to build commit in repository: " + name, e);
        }
    }

    private DirCache stageAll(boolean onlyStageFilesAlreadyTracked) {
        return applyStagingAction(add -> add.addFilepattern(ALL_FILES_PATTERN).setUpdate(onlyStageFilesAlreadyTracked));
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

    private CommitBuilder configureCommitBuilder(
            String message,
            ObjectId indexTreeId,
            @Nullable ObjectId parent
    ) {
        var commitBuilder = new CommitBuilder();
        if (parent != null) {
            commitBuilder.setParentId(parent);
        }

        var commitAuthor = new PersonIdent(repository);
        commitBuilder.setCommitter(commitAuthor);
        commitBuilder.setAuthor(commitAuthor);
        commitBuilder.setMessage(message);
        commitBuilder.setTreeId(indexTreeId);
        return commitBuilder;
    }

    private void updateRef(
            String referenceToUpdate,
            RevCommit revCommit,
            ObjectId commitId,
            @Nullable ObjectId parent
    ) throws IOException {
        var parentId = parent == null ? ObjectId.zeroId() : parent;

        var refUpdate = repository.updateRef(referenceToUpdate);
        refUpdate.setNewObjectId(commitId);
        String prefix = parent == null ? "commit (initial): " : "commit: ";
        refUpdate.setRefLogMessage(prefix + revCommit.getShortMessage(), false);
        refUpdate.setExpectedOldObjectId(parentId);

        RefUpdate.Result rc = refUpdate.update();

        switch (rc) {
            case NEW:
            case FORCED:
            case FAST_FORWARD: {
                break;
            }
            case REJECTED:
            case LOCK_FAILURE:
                throw new RepositoryVersioningException("Failed to update reference '" + referenceToUpdate +
                        "' in repository '" + name + "': " + JGitText.get().couldNotLockHEAD);
            default:
                throw new RepositoryVersioningException("Failed to update reference '" + referenceToUpdate +
                        "' in repository '" + name + "': " + JGitText.get().updatingRefFailed);
        }
    }
}
