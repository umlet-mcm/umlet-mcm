package at.ac.tuwien.model.change.management.git.infrastructure;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.TestRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class ManagedDiffFormatterTest {

    @TempDir
    private Path tempDir;

    private Repository repository;

    private TestRepository<Repository> testRepository;

    private ManagedDiffFormatter managedDiffFormatter;

    private final static String REPOSITORY_NAME = "test-repo";

    private final static String GIT_DIR = ".git";

    private final static Charset ENCODING = StandardCharsets.UTF_8;

    @BeforeEach
    public void setup() throws IOException {
        repository = getJGitRepository();
        initTestRepository();
        testRepository = new TestRepository<>(repository);
        managedDiffFormatter = new ManagedDiffFormatter(repository, StandardCharsets.UTF_8, REPOSITORY_NAME);
    }

    @AfterEach
    public void tearDown() throws IOException {
        testRepository.close();
        repository.close();
        managedDiffFormatter.close();
    }

    @Test
    public void testCreateUnifiedDiff_noFilesCommitted_returnsEmptyList() throws Exception {
        var firstCommit = testRepository.commit().create();
        var secondCommit = testRepository.commit().parent(firstCommit).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).isEmpty();
    }

    @Test
    public void testCreateUnifiedDiff_unchangedFile_shouldReturnUnchangedDiffEntry() throws Exception {
        var firstCommit = testRepository.commit().add("file.txt", "content").create();
        var secondCommit = testRepository.commit().parent(firstCommit).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement().isInstanceOf(ManagedDiffEntry.Unchanged.class);
    }

    @Test
    public void testCreateUnifiedDiff_unchangedFiles_unchangedNotIncluded_shouldReturnEmptyList() throws Exception {
        var firstCommit = testRepository.commit()
                .add("file1.txt", "content")
                .add("file2.txt", "content")
                .add("file3.txt", "content")
                .create();
        var secondCommit = testRepository.commit().parent(firstCommit).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, false);

        Assertions.assertThat(diffs).isEmpty();
    }


    @Test
    public void testCreateUnifiedDiff_addedFile_shouldReturnAddDiffEntry() throws Exception {
        var firstCommit = testRepository.commit().create();
        var secondCommit = testRepository.commit().add("file.txt", "content").create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement().isInstanceOf(ManagedDiffEntry.Add.class);
    }

    @Test
    public void testCreateUnifiedDiff_threeAddedFiles_shouldReturnThreeAddDiffEntries() throws Exception {
        var firstCommit = testRepository.commit().create();
        var secondCommit = testRepository.commit()
                .add("file1.txt", "content")
                .add("file2.txt", "content")
                .add("file3.txt", "content")
                .create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).hasSize(3)
                .allMatch(diff -> diff instanceof ManagedDiffEntry.Add);
    }

    @Test
    public void testCreateUnifiedDiff_modifiedFile_shouldReturnModifyDiffEntry() throws Exception {
        var filePath = "file.txt";
        var firstCommit = testRepository.commit().add(filePath, "content").create();
        var secondCommit = testRepository.commit().parent(firstCommit).add(filePath, "new content").create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement().isInstanceOf(ManagedDiffEntry.Modify.class);
    }

    @Test
    public void testCreateUnifiedDiff_threeModifiedFiles_shouldReturnThreeModifyDiffEntries() throws Exception {
        var firstCommit = testRepository.commit()
                .add("file1.txt", "content")
                .add("file2.txt", "content").add("file3.txt", "content")
                .create();
        var secondCommit = testRepository.commit().parent(firstCommit)
                .add("file1.txt", "new content")
                .add("file2.txt", "new content")
                .add("file3.txt", "new content")
                .create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).hasSize(3)
                .allMatch(diff -> diff instanceof ManagedDiffEntry.Modify);
    }

    @Test
    public void testCreateUnifiedDiff_deletedFile_shouldReturnDeleteDiffEntry() throws Exception {
        var filePath = "file.txt";
        var firstCommit = testRepository.commit().add(filePath, "content").create();
        var secondCommit = testRepository.commit().parent(firstCommit).rm(filePath).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement().isInstanceOf(ManagedDiffEntry.Delete.class);
    }

    @Test
    public void testCreateUnifiedDiff_threeDeletedFiles_shouldReturnThreeDeleteDiffEntries() throws Exception {
        var firstCommit = testRepository.commit()
                .add("file1.txt", "content")
                .add("file2.txt", "content")
                .add("file3.txt", "content")
                .create();
        var secondCommit = testRepository.commit().parent(firstCommit)
                .rm("file1.txt")
                .rm("file2.txt")
                .rm("file3.txt")
                .create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).hasSize(3)
                .allMatch(diff -> diff instanceof ManagedDiffEntry.Delete);
    }

    @Test
    public void testCreateUnifiedDiff_renameFile_shouldReturnDeleteAndAddEntries() throws Exception {
        var fileContent = "content";
        var oldFilePath = "old-file.txt";
        var newFilePath = "new-file.txt";

        var firstCommit = testRepository.commit().add(oldFilePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).rm(oldFilePath).add(newFilePath, fileContent).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).hasSize(2)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Delete)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Add);
    }

    @Test
    public void testCreateUnifiedDiff_copiedFile_shouldReturnAddAndUnchangedEntry() throws Exception {
        var fileContent = "content";

        var firstCommit = testRepository.commit().add("file.txt", fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).add("copied-file.txt", fileContent).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).hasSize(2)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Add)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Unchanged);
    }

    @Test
    public void testCreateUnifiedDiff_differentFileOperations_shouldReturnCorrectDiffEntries() throws Exception {
        var firstCommit = testRepository.commit()
                .add("unchanged-file.txt", "unchanged-content")
                .add("modified-file.txt", "modified-content")
                .add("deleted-file.txt", "deleted-content")
                .create();
        var secondCommit = testRepository.commit().parent(firstCommit)
                .add("modified-file.txt", "new-content")
                .rm("deleted-file.txt")
                .add("added-file.txt", "added-content")
                .create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).hasSize(4)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Unchanged)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Modify)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Delete)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Add);
    }

    @Test
    public void testCreateUnifiedDiff_differentFileOperations_unchangedNotIncluded_shouldReturnCorrectDiffEntries() throws Exception {
        var firstCommit = testRepository.commit()
                .add("unchanged-file.txt", "unchanged-content")
                .add("modified-file.txt", "modified-content")
                .add("deleted-file.txt", "deleted-content")
                .create();
        var secondCommit = testRepository.commit().parent(firstCommit)
                .add("modified-file.txt", "new-content")
                .rm("deleted-file.txt")
                .add("added-file.txt", "added-content")
                .create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, false);

        Assertions.assertThat(diffs).hasSize(3)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Modify)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Delete)
                .anyMatch(diff -> diff instanceof ManagedDiffEntry.Add);
    }

    @Test
    public void testUnifiedDiffOutput_unchangedFile_shouldReturnFileContentAsDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content\n";
        var firstCommit = testRepository.commit().add(filePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualTo(fileContent);
    }

    @Test
    public void testUnfiiedDiffOutput_unchangedFile_preprocessed_shouldReturnFileContentAfterPreprocessing() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content\n";
        var firstCommit = testRepository.commit().add(filePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject -> {
            var objectContent = managedRepositoryObject.getFileContent();
            return objectContent.replace("content", "changed").getBytes(ENCODING);
        });

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualTo("changed\n");
    }

    @Test
    public void testUnifiedDiffOutput_unchangedFile_preprocessorReturningNull_shouldReturnFileContentAsDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content\n";
        var firstCommit = testRepository.commit().add(filePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject -> null);

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualTo(fileContent);
    }

    @Test
    public void testUnifiedDiffOutput_unchangedFile_outputPreprocssedAway_shouldNotReturnAnyDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content\n";
        var firstCommit = testRepository.commit().add(filePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject -> "".getBytes(ENCODING));

        Assertions.assertThat(diffs).isEmpty();
    }

    @Test
    public void testUnifiedDiffOutput_addedFile_shouldReturnAddDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content\n";
        var firstCommit = testRepository.commit().create();
        var secondCommit = testRepository.commit().add(filePath, fileContent).create();

        var diffFormatString = """
                diff --git a/%s b/%s
                new file mode 100644
                index %s..%s
                --- /dev/null
                +++ b/%s
                @@ -0,0 +1 @@
                +%s
                """;

        var expectedDiff = diffFormatString.formatted(
                filePath, filePath, ObjectId.zeroId().getName(), objectId(filePath, fileContent), filePath, fileContent.strip()
        );

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualToIgnoringWhitespace(expectedDiff);
    }

    @Test
    public void testUnifiedDiffOutput_addedFile_preprocessorReturningNull_shouldReturnAddDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content\n";
        var firstCommit = testRepository.commit().create();
        var secondCommit = testRepository.commit().add(filePath, fileContent).create();

        var diffFormatString = """
                diff --git a/%s b/%s
                new file mode 100644
                index %s..%s
                --- /dev/null
                +++ b/%s
                @@ -0,0 +1 @@
                +%s
                """;

        var expectedDiff = diffFormatString.formatted(
                filePath, filePath, ObjectId.zeroId().getName(), objectId(filePath, fileContent), filePath, fileContent.strip()
        );

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, mangagedRepositoryObject -> null);

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualToIgnoringWhitespace(expectedDiff);
    }

    @Test
    public void testCreateUnifiedDiff_addedFile_preprocessed_shouldReturnAddDiffOutputAfterPreprocessing() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content...deleted\n";
        var firstCommit = testRepository.commit().create();
        var secondCommit = testRepository.commit().add(filePath, fileContent).create();

        var diffFormatString = """
                diff --git a/%s b/%s
                new file mode 100644
                index %s..%s
                --- /dev/null
                +++ b/%s
                @@ -0,0 +1 @@
                +%s
                """;

        var expectedDiff = diffFormatString.formatted(
                filePath, filePath, ObjectId.zeroId().getName(), objectId(filePath, fileContent), filePath, "content..."
        );

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject -> {
            var objectContent = managedRepositoryObject.getFileContent();
            return objectContent.replace("deleted", "").getBytes(ENCODING);
        });

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualToIgnoringWhitespace(expectedDiff);
    }

    @Test
    public void testCreateUnifiedDiffOutput_addedFile_changesPreProcessedAway_shouldNotReturnAnyDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content...deleted\n";
        var firstCommit = testRepository.commit().create();
        var secondCommit = testRepository.commit().add(filePath, fileContent).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject ->
                "".getBytes(ENCODING)
        );

        Assertions.assertThat(diffs).isEmpty();
    }

    @Test
    public void testUnifiedDiffOutput_modifiedFile_shouldReturnModifyDiffOutput() throws Exception {
        var filePath = "file.txt";
        var oldContent = "content\n";
        var newContent = "new content\n";
        var firstCommit = testRepository.commit().add(filePath, oldContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).add(filePath, newContent).create();


        var diffFormatString = """
                diff --git a/%s b/%s
                index %s..%s
                --- a/%s
                +++ b/%s
                @@ -1 +1 @@
                -%s
                +%s
                """;


        var expectedDiff = diffFormatString.formatted(
                filePath, filePath, objectId(filePath, oldContent), objectId(filePath, newContent),
                filePath, filePath, oldContent.strip(), newContent.strip()
        );

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement().extracting(ManagedDiffEntry::getDiff, STRING)
                .satisfies(diff -> Assertions.assertThat(removeIndexFileMode(diff)).isEqualToIgnoringWhitespace(expectedDiff));
    }

    @Test
    public void testUnifiedDiffOutput_modifiedFile_preprocessed_shouldReturnModifyDiffOutputAfterPreprocessing() throws Exception {
        var filePath = "file.txt";
        var oldContent = "content...deleted\n";
        var newContent = "new content\n";
        var firstCommit = testRepository.commit().add(filePath, oldContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).add(filePath, newContent).create();

        var diffFormatString = """
                diff --git a/%s b/%s
                index %s..%s
                --- a/%s
                +++ b/%s
                @@ -1 +1 @@
                -%s
                +%s
                """;

        var expectedDiff = diffFormatString.formatted(
                filePath, filePath, objectId(filePath, oldContent), objectId(filePath, newContent),
                filePath, filePath, "content...", newContent.strip()
        );

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject -> {
            var objectContent = managedRepositoryObject.getFileContent();
            return objectContent.replace("deleted", "").getBytes(ENCODING);
        });

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .satisfies(diff -> Assertions.assertThat(removeIndexFileMode(diff)).isEqualToIgnoringWhitespace(expectedDiff));
    }

    @Test
    public void testUnifiedDiffOuptut_modifiedFile_changesPreProcessedAway_shouldNotReturnAnyDiffOutput() throws Exception {
        var filePath = "file.txt";
        var oldContent = "content...changed\n";
        var newContent = "content...modified\n";
        var firstCommit = testRepository.commit().add(filePath, oldContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).add(filePath, newContent).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject -> {
            var objectContent = managedRepositoryObject.getFileContent();
            return objectContent.replace("changed", "").replace("modified", "").getBytes(ENCODING);
        });

        Assertions.assertThat(diffs).isEmpty();
    }

    @Test
    public void testUnifiedDiffOutput_deletedFile_shouldReturnDeletedDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content\n";
        var firstCommit = testRepository.commit().add(filePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).rm(filePath).create();

        var diffFormatString = """
                diff --git a/%s b/%s
                deleted file mode 100644
                index %s..%s
                --- a/%s
                +++ /dev/null
                @@ -1 +0,0 @@
                -%s
                """;

        var expectedDiff = diffFormatString.formatted(
                filePath, filePath, objectId(filePath, fileContent), ObjectId.zeroId().getName(), filePath, fileContent.strip()
        );

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true);

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualToIgnoringWhitespace(expectedDiff);
    }

    @Test
    public void testUnifiedDiffOutput_deletedFile_preprocessed_shouldReturnDeletedDiffOutputAfterPreprocessing() throws Exception {
        var filePath = "file.txt";
        var fileContent = "content...deleted\n";
        var firstCommit = testRepository.commit().add(filePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).rm(filePath).create();

        var diffFormatString = """
                diff --git a/%s b/%s
                deleted file mode 100644
                index %s..%s
                --- a/%s
                +++ /dev/null
                @@ -1 +0,0 @@
                -%s
                """;

        var expectedDiff = diffFormatString.formatted(
                filePath, filePath, objectId(filePath, fileContent), ObjectId.zeroId().getName(), filePath, "content..."
        );

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true, managedRepositoryObject -> {
            var objectContent = managedRepositoryObject.getFileContent();
            return objectContent.replace("deleted", "").getBytes(ENCODING);
        });

        Assertions.assertThat(diffs).singleElement()
                .extracting(ManagedDiffEntry::getDiff, STRING)
                .isEqualToIgnoringWhitespace(expectedDiff);
    }

    @Test
    public void testUnifiedDiffOutput_deletedFile_changesPreProcessedAway_shouldNotReturnAnyDiffOutput() throws Exception {
        var filePath = "file.txt";
        var fileContent = "to-delete\n";
        var firstCommit = testRepository.commit().add(filePath, fileContent).create();
        var secondCommit = testRepository.commit().parent(firstCommit).rm(filePath).create();

        var diffs = managedDiffFormatter.createUnifiedDiff(firstCommit, secondCommit, true,
                managedRepositoryObject -> "".getBytes(ENCODING));

        Assertions.assertThat(diffs).isEmpty();
    }

    private String removeIndexFileMode(String diffOutput) {
        var indexLinePattern = "^(index \\S+\\.\\.\\S+) \\d+$";
        return diffOutput.lines()
                .map(line -> {
                    if (line.matches(indexLinePattern)) {
                        return line.replaceAll(indexLinePattern, "$1");
                    }
                    return line;
                }).collect(Collectors.joining(System.lineSeparator()));
    }

    private String objectId(String path, String content) throws Exception {
        var blob = testRepository.blob(content.getBytes(ENCODING));
        return testRepository.file(path, blob).getObjectId().getName();
    }

    private Repository getJGitRepository() throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(getWorkDir().resolve(GIT_DIR).toFile())
                .build();
    }

    private void initTestRepository() {
        initRepository(repository);
    }

    @SneakyThrows(GitAPIException.class)
    private void initRepository(Repository repository) {
        var git = Git.init().setDirectory(repository.getWorkTree()).call();
        git.close();
    }

    private Path getWorkDir() {
        return tempDir.resolve(REPOSITORY_NAME);
    }
}
