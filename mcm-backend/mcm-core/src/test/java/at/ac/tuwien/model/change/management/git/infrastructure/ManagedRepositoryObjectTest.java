package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.junit.TestRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ManagedRepositoryObjectTest {

    private final Charset defaultEncoding = StandardCharsets.UTF_8;

    @Test
    public void testGetEncoding_shouldReturnCorrectEncoding() {
        try (var repository = getJGitRepository()) {
            var object = getManagedRepositoryObject(repository, "testFile", "content");
            Assertions.assertThat(object.getEncoding()).isEqualTo(defaultEncoding);
        }
    }

    @Test
    public void testGetRawFilePath_shouldReturnCorrectPath() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFile";
            var object = getManagedRepositoryObject(repository, actualPath, "content");
            Assertions.assertThat(object.getRawFilePath()).isEqualTo(actualPath.getBytes(defaultEncoding));
        }
    }

    @Test
    public void testGetFilePath_shouldReturnCorrectPath() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFile";
            var object = getManagedRepositoryObject(repository, actualPath, "content");
            Assertions.assertThat(object.getFilePath()).isEqualTo(actualPath);
        }
    }

    @Test
    public void testGetFilePath_differentEncoding_shouldReturnCorrectPath() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFïle";
            var object = getManagedRepositoryObject(repository, actualPath, "content", StandardCharsets.UTF_16);
            Assertions.assertThat(object.getFilePath()).isEqualTo(actualPath);
        }
    }

    @Test
    public void testGetRawFileContent_shouldReturnCorrectContent() {
        try(var repository = getJGitRepository()) {
            var actualContent = "content";
            var object = getManagedRepositoryObject(repository, "testFile", actualContent);
            Assertions.assertThat(object.getRawFileContent()).isEqualTo(actualContent.getBytes(defaultEncoding));
        }
    }

    @Test
    public void testGetRawFileContent_emptyFileContent_shouldReturnEmptyContent() {
        try(var repository = getJGitRepository()) {
            var actualContent = "";
            var object = getManagedRepositoryObject(repository, "testFile", actualContent);
            Assertions.assertThat(object.getRawFileContent()).isEmpty();
        }
    }

    @Test
    public void testGetRawFileContent_loaderIsNull_shouldThrowRepositoryReadException() throws IOException {
        try (var repository = spy(getJGitRepository())) {
            doReturn(null).when(repository).open(any());
            var object = getManagedRepositoryObject(repository, "testFile", "content");
            Assertions.assertThatThrownBy(object::getRawFileContent).isInstanceOf(RepositoryReadException.class);
        }
    }

    @Test
    public void testGetRawFileContent_openThrowsIOException_shouldThrowRepositoryReadException() throws IOException {
        try (var repository = spy(getJGitRepository())) {
            doThrow(new IOException()).when(repository).open(any());
            var object = getManagedRepositoryObject(repository, "testFile", "content");
            Assertions.assertThatThrownBy(object::getRawFileContent).isInstanceOf(RepositoryReadException.class);
        }
    }

    @Test
    public void testGetFileContent_shouldReturnCorrectContent() {
        try(var repository = getJGitRepository()) {
            var actualContent = "content";
            var object = getManagedRepositoryObject(repository, "testFile", actualContent);
            Assertions.assertThat(object.getFileContent()).isEqualTo(actualContent);
        }
    }

    @Test
    public void testGetFileContent_differentEncoding_shouldReturnCorrectContent() {
        try(var repository = getJGitRepository()) {
            var actualContent = "contänt";
            var object = getManagedRepositoryObject(repository, "testFile", actualContent, StandardCharsets.US_ASCII);
            Assertions.assertThat(object.getFileContent()).isEqualTo(actualContent);
        }
    }

    @Test
    public void testGetFileContent_emptyFileContent_shouldReturnEmptyContent() {
        try(var repository = getJGitRepository()) {
            var actualContent = "";
            var object = getManagedRepositoryObject(repository, "testFile", actualContent);
            Assertions.assertThat(object.getFileContent()).isEmpty();
        }
    }

    @Test
    @SuppressWarnings("resource")
    public void testConstructor_treeWalkReturnsZeroId_shouldThrowRepositoryReadException() {
        var mockTreeWalk = mock(TreeWalk.class);
        var mockRepository = mock(Repository.class);
        when(mockTreeWalk.getRawPath()).thenReturn("testFile".getBytes(defaultEncoding));
        when(mockTreeWalk.getObjectId(0)).thenReturn(ObjectId.zeroId());
        Assertions.assertThatThrownBy(() -> new ManagedRepositoryObject(mockTreeWalk, mockRepository, defaultEncoding))
                .isInstanceOf(RepositoryReadException.class);
    }

    @Test
    public void testRawFilePathMatches_correctPath_shouldReturnTrue() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFile";
            var object = getManagedRepositoryObject(repository, actualPath, "content");
            Assertions.assertThat(object.rawFilePathMatches(actualPath.getBytes(defaultEncoding))).isTrue();
        }
    }

    @Test
    public void testRawFilePathMatches_incorrectPath_shouldReturnFalse() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFile";
            var object = getManagedRepositoryObject(repository, actualPath, "content");
            Assertions.assertThat(object.rawFilePathMatches("differentPath".getBytes(defaultEncoding))).isFalse();
        }
    }

    @Test
    public void testRawFilePathMatches_partialCorrectPath_shouldReturnTrue() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFile";
            var object = getManagedRepositoryObject(repository, actualPath, "content");
            Assertions.assertThat(object.rawFilePathMatches("test".getBytes(defaultEncoding))).isTrue();
        }
    }

    @Test
    public void testConstructor_constructionPerObjectID_correctlyAssignsFields() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFile";
            var actualContent = "content";
            var treeWalk = getTreeWalkForPath(repository, actualPath, actualContent);
            var objectID = treeWalk.getObjectId(0);

            var object = new ManagedRepositoryObject(objectID, actualPath, repository, defaultEncoding, false);
            Assertions.assertThat(object.getEncoding()).isEqualTo(defaultEncoding);
            Assertions.assertThat(object.getRawFilePath()).isEqualTo(actualPath.getBytes(defaultEncoding));
            Assertions.assertThat(object.getFilePath()).isEqualTo(actualPath);
            Assertions.assertThat(object.getRawFileContent()).isEqualTo(actualContent.getBytes(defaultEncoding));
            Assertions.assertThat(object.getFileContent()).isEqualTo(actualContent);
        }
    }

    @Test
    public void testConstructor_objectIdConstructor_withCaching_correctlyAssignsFields() {
        try (var repository = getJGitRepository()) {
            var actualPath = "testFile";
            var actualContent = "content";
            var treeWalk = getTreeWalkForPath(repository, actualPath, actualContent);
            var objectID = treeWalk.getObjectId(0);

            var object = new ManagedRepositoryObject(objectID, actualPath, repository, defaultEncoding, true);
            Assertions.assertThat(object.getEncoding()).isEqualTo(defaultEncoding);
            Assertions.assertThat(object.getRawFilePath()).isEqualTo(actualPath.getBytes(defaultEncoding));
            Assertions.assertThat(object.getFilePath()).isEqualTo(actualPath);
            Assertions.assertThat(object.getRawFileContent()).isEqualTo(actualContent.getBytes(defaultEncoding));
            Assertions.assertThat(object.getFileContent()).isEqualTo(actualContent);
        }
    }

    @Test
    public void testGetRawFilePath_rawFilePathAndFilePathAreNull_shouldThrowRepositoryReadException() {
        try (var repository = getJGitRepository()) {
            var treeWalk = mock(TreeWalk.class);
            when(treeWalk.getObjectId(0)).thenReturn(new ObjectId(0, 0, 0, 0, 1));
            when(treeWalk.getRawPath()).thenReturn(null);
            var object = new ManagedRepositoryObject(treeWalk, repository, defaultEncoding);
            Assertions.assertThatThrownBy(object::getRawFilePath).isInstanceOf(RepositoryReadException.class);
        }
    }

    @Test
    public void testGetFilePath_rawFilePathAndFilePathAreNull_shouldThrowRepositoryReadException() {
        try (var repository = getJGitRepository()) {
            var treeWalk = mock(TreeWalk.class);
            when(treeWalk.getObjectId(0)).thenReturn(new ObjectId(0, 0, 0, 0, 1));
            when(treeWalk.getRawPath()).thenReturn(null);
            var object = new ManagedRepositoryObject(treeWalk, repository, defaultEncoding);
            Assertions.assertThatThrownBy(object::getFilePath).isInstanceOf(RepositoryReadException.class);
        }
    }

    @Test
    public void testGetRawFileContent_cachingEnabled_shouldOnlyComputeContentOnce() throws IOException {
        try (var repository = spy(getJGitRepository())) {
            var treeWalk = getTreeWalkForPath(repository, "actualPath", "actualContent");
            var objectID = treeWalk.getObjectId(0);
            var object = new ManagedRepositoryObject(objectID, treeWalk.getPathString(), repository, defaultEncoding, true);
            var firstCall = object.getRawFileContent();
            var secondCall = object.getRawFileContent();
            verify(repository, times(1)).open(any());
            Assertions.assertThat(firstCall).isEqualTo(secondCall);
        }
    }

    @Test
    public void testGetFileContent_cachingEnabled_shouldOnlyComputeContentOnce() throws IOException {
        try (var repository = spy(getJGitRepository())) {
            var treeWalk = getTreeWalkForPath(repository, "actualPath", "actualContent");
            var objectID = treeWalk.getObjectId(0);
            var object = new ManagedRepositoryObject(objectID, treeWalk.getPathString(), repository, defaultEncoding, true);
            var firstCall = object.getFileContent();
            var secondCall = object.getFileContent();
            verify(repository, times(1)).open(any());
            Assertions.assertThat(firstCall).isEqualTo(secondCall);
        }
    }

    private InMemoryRepository getJGitRepository() {
        var repoDesc = new DfsRepositoryDescription("testRepo");
        return new InMemoryRepository(repoDesc);
    }

    private ManagedRepositoryObject getManagedRepositoryObject(Repository repository, String path, String content) {
        return getManagedRepositoryObject(repository, path, content, defaultEncoding);
    }

    @SneakyThrows
    private ManagedRepositoryObject getManagedRepositoryObject(Repository repository, String path, String content, Charset encoding) {
        var treeWalk = getTreeWalkForPath(repository, path, content);
        return new ManagedRepositoryObject(treeWalk, repository, encoding);
    }

    @SneakyThrows
    private TreeWalk getTreeWalkForPath(Repository repository, String path, String content) {
        var testRepository = new TestRepository<>(repository);
        var commit = testRepository.commit().add(path, content).create();
        return TreeWalk.forPath(testRepository.getRepository(), path, commit.getTree());
    }
}
