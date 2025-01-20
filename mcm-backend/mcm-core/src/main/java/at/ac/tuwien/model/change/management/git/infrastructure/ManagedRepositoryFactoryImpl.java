package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.util.FS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@GitComponent
@Slf4j
@RequiredArgsConstructor
public class ManagedRepositoryFactoryImpl implements ManagedRepositoryFactory {
    private static final String GIT_DIRECTORY = ".git";

    private final GitProperties properties;

    @Override
    public ManagedRepository getRepositoryByName(@NonNull String name) {
        return getManagedRepositoryByName(name);
    }

    @Override
    public List<ManagedRepository> getAllRepositories() {
        var repositoriesDir = getRepositoriesDir();
        try (var fileStream = Files.list(repositoriesDir)) {
            log.debug("Listing repositories in '{}'", repositoriesDir);
            var repositories = fileStream
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(this::getManagedRepositoryByName)
                    .filter(ManagedRepository::exists)
                    .toList();
            log.debug("Found {} repositories in '{}'", repositories.size(), repositoriesDir);
            return repositories;
        } catch (IOException e) {
            throw new RepositoryAccessException("Failed to list repositories in: '" + repositoriesDir, e);
        }
    }

    private ManagedRepository getManagedRepositoryByName(String name) {
        try {
            var jGitRepository = getJGitRepositoryByPath(resolveRepositoryPath(name));
            if (jGitRepository.getWorkTree() == null) {
                throw new RepositoryAccessException("No working directory found for repository of name: " + name);
            }
            var workDir = jGitRepository.getWorkTree().toPath();
            var managedRepository = initializeManagedRepository(jGitRepository, name, workDir);
            log.debug("Built repository: {}", managedRepository.getName());
            return managedRepository;
        } catch (IOException e) {
            throw new RepositoryAccessException("Failed to access repository: " + name, e);
        }
    }

    private ManagedRepository initializeManagedRepository(Repository jGitRepository, String name, Path workDir) {
        return new ManagedRepository(
                jGitRepository,
                name,
                properties.getEncoding(),
                new ManagedRepositoryVersioning(jGitRepository, name, properties.getEncoding(), workDir),
                workDir
        );
    }

    private Path resolveRepositoryPath(String name) {
        var repositoriesDir = getRepositoriesDir();
        var repositoryPath = repositoriesDir.resolve(name).normalize();
        if (! repositoryPath.startsWith(repositoriesDir)) {
            throw new RepositoryAccessException("Attempted to access repository outside of: " + repositoriesDir);
        }
        return repositoryPath;
    }

    private Path getRepositoriesDir() {
        var repositoriesDir = properties.getRepositories();
        if (!repositoriesDir.toFile().exists()) {
            throw new RepositoryAccessException("Repositories directory specified in Git properties does not exist");
        }
        return repositoriesDir;
    }


    private static Repository getJGitRepositoryByPath(Path repositoryPath) throws IOException {
        var gitPath = repositoryPath.endsWith(GIT_DIRECTORY)
                ? repositoryPath
                : repositoryPath.resolve(GIT_DIRECTORY);
        var key = RepositoryCache.FileKey.lenient(gitPath.toFile(), FS.DETECTED);
        return new RepositoryBuilder()
                .setFS(FS.DETECTED)
                .setGitDir(key.getFile())
                .setMustExist(false)
                .build();
    }
}
