package at.ac.tuwien.model.change.management.testutils.git;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.operation.RepositoryManager;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MockRepositoryManager implements RepositoryManager {

    // would be nicer to test without the file system
    // but that would require mocking a ton of things
    private final Path testDirectory;

    public MockRepositoryManager(Path testDirectory) {
        this.testDirectory = testDirectory;
    }

    @Override
    public Repository accessRepository(String repositoryName) {
        try {
            var repositoryDir = testDirectory.resolve(repositoryName);
            return RepositoryUtils.getRepositoryAtPath(repositoryDir);
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not access repository '" + repositoryName + "'", e);
        }
    }

    @Override
    public List<Repository> listRepositories() {
        try(var fileStream = Files.list(testDirectory)) {
            return fileStream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(this::accessRepository)
                    .toList();
        } catch (IOException e) {
            throw new RepositoryAccessException("Could not list directories in '" + testDirectory + "'", e);
        }
    }
}
