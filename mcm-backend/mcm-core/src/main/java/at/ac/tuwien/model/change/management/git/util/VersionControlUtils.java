package at.ac.tuwien.model.change.management.git.util;

import lombok.NonNull;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

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
        stagePaths(repository, contents.models());
        stagePaths(repository, contents.nodes());
        stagePaths(repository, contents.relations());
    }

    public static void stagePaths(
            @NonNull Repository repository,
            @NonNull Collection<Path> paths) throws GitAPIException {
        if (paths.isEmpty()) return;
        var addCommand = new AddCommand(repository);
        var workDir = repository.getWorkTree().toPath();
        paths.forEach(path -> addCommand.addFilepattern(workDir.relativize(path).toString()));
        addCommand.call();
    }

    public static void commitRepository(@NonNull Repository repository, @NonNull String message) throws GitAPIException {
        var commitCommand = Git.wrap(repository).commit().setAll(true);
        commitCommand.setMessage(message);
        commitCommand.call();
    }


}
