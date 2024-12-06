package at.ac.tuwien.model.change.management.git.util;

import lombok.NonNull;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static at.ac.tuwien.model.change.management.core.utils.PathUtils.normalizePath;

public final class VersionControlUtils {

    private VersionControlUtils(){}

    public static void initRepository(@NonNull Repository repository) throws GitAPIException {
        var repositoryDir = Optional.of(repository.getDirectory())
                .orElseThrow(() -> new IllegalArgumentException("Repository directory is not set"))
                .toPath();
        // some strange behavior here but apparently we have to setDirectory to the work tree
        // rather than set the .git directory
        // doing the latter means that workTree will be set to the project's working directory
        // which we definitely don't want
        var workDirectory = repositoryDir.endsWith(GitConstants.GIT_DIRECTORY)
                ? repositoryDir.getParent()
                : repositoryDir;
        var git = Git.init().setDirectory(workDirectory.toFile()).call();
        git.close();
    }

    public static void stageRepositoryContents(
            @NonNull Repository repository,
            @NonNull RepositoryContents<Path> contents) throws GitAPIException {
        stagePaths(
                repository,
                Stream.of(contents.models(), contents.nodes(), contents.relations()).flatMap(Collection::stream).toList()
        );
    }

    public static void stagePaths(
            @NonNull Repository repository,
            @NonNull Collection<Path> paths) throws GitAPIException {
        if (paths.isEmpty()) return;
        stage(repository, addCommand -> {
            var workDir = repository.getWorkTree().toPath();
            paths.forEach(path -> addCommand.addFilepattern(
                    normalizePath(workDir.relativize(path).toString())
            ));
        });
    }

    // also stages untracked files
    public static void stageAllChanges(@NonNull Repository repository) throws GitAPIException {
        stage(repository, addCommand -> addCommand.addFilepattern("."));
    }

    public static void commitRepository(@NonNull Repository repository,
                                        @NonNull String message,
                                        boolean autoStageModifiedFiles) throws GitAPIException {
        var commitCommand = Git.wrap(repository).commit().setAll(autoStageModifiedFiles);
        commitCommand.setMessage(message);
        commitCommand.call();
    }

    private static void stage(@NonNull Repository repository, @NonNull Consumer<AddCommand> addAction)
            throws GitAPIException {
        var addCommand = new AddCommand(repository);
        addAction.accept(addCommand);
        addCommand.call();
    }
}
