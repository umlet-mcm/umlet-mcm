package at.ac.tuwien.model.change.management.git.util;

import lombok.NonNull;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;


public final class RepositoryUtils {

    private RepositoryUtils() {}

    @FunctionalInterface
    public interface TreeWalkConsumer {
        void accept(TreeWalk treeWalk) throws IOException;
    }

    public static Repository getRepositoryAtPath(@NonNull Path repositoryPath) throws IOException {
        var gitPath = repositoryPath.endsWith(GitConstants.GIT_DIRECTORY)
                ? repositoryPath
                : repositoryPath.resolve(GitConstants.GIT_DIRECTORY);
        var key = RepositoryCache.FileKey.lenient(gitPath.toFile(), FS.DETECTED);
        return new RepositoryBuilder()
                .setFS(FS.DETECTED)
                .setGitDir(key.getFile())
                .setMustExist(false)
                .build();
    }

    public static boolean repositoryExists(@NonNull Repository repository) {
        return repository.getObjectDatabase() != null && repository.getObjectDatabase().exists();
    }

    public static String getRepositoryName(@NonNull Repository repository) {
        return repository.getWorkTree().getName();
    }

    public static Optional<AnyObjectId> resolveCommit(@NonNull Repository repository, String commitRef) throws IOException {
        return Optional.ofNullable(repository.resolve(commitRef));
    }

    public static void walkCommit(
            @NonNull Repository repository,
            @NonNull RevCommit revCommit,
            @NonNull TreeWalkConsumer action
    ) throws IOException {
        walkCommit(repository, revCommit, null, action);
    }

    public static void walkCommit(
            @NonNull Repository repository,
            @NonNull RevCommit commit,
            @Nullable TreeFilter filter,
            @NonNull TreeWalkConsumer action
            ) throws IOException {
        try(var treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            if (filter != null) treeWalk.setFilter(filter);
            while (treeWalk.next()) {
                action.accept(treeWalk);
            }
        }
    }

    public static Optional<byte[]> getTreeWalkContent(@NonNull TreeWalk treeWalk) {
        return Optional.ofNullable(treeWalk.getObjectReader())
                .flatMap(reader -> getObjectLoader(reader, treeWalk.getObjectId(0)))
                .map(ObjectLoader::getCachedBytes);
    }

    public static boolean treeWalkIsInDirectory(@NonNull TreeWalk treeWalk, byte[] rawDirectoryName) {
        return treeWalk.isPathPrefix(rawDirectoryName, rawDirectoryName.length) == 0;
    }

    private static Optional<ObjectLoader> getObjectLoader(ObjectReader reader, ObjectId objectId) {
        try {
            return Optional.ofNullable(reader.open(objectId));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
