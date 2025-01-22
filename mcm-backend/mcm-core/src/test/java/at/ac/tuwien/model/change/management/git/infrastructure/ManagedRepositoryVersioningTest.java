package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryVersioningException;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.junit.TestRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManagedRepositoryVersioningTest {

    @TempDir
    private Path tempDir;

    private static final String testRepo = "testRepository";

    private static final Charset encoding = StandardCharsets.UTF_8;

    private Repository repository;

    private ManagedRepositoryVersioning versioning;

    private static final String REF_MAIN = "main";
    private static final String REF_HEAD = "HEAD";

    @BeforeEach
    public void setup() throws IOException {
        repository = getJGitRepository();
        versioning = getManagedRepositoryVersioning(repository);
    }

    @AfterEach
    public void cleanup() {
        repository.close();
    }

    @Test
    public void testInit_shouldCreateRepository() throws IOException {
        Assertions.assertThat(repositoryExists(repository)).isFalse();
        versioning.init();
        Assertions.assertThat(repositoryExists(repository)).isTrue();
        Assertions.assertThat(repository.getBranch()).isEqualTo(REF_MAIN);
    }

    @Test
    public void testStageFiles_zeroFiles_shouldReturnZero() {
        versioning.init();
        var stagedFiles = versioning.stageFiles(Collections.emptyList());
        Assertions.assertThat(stagedFiles).isEqualTo(0);
    }

    @Test
    public void testStageFiles_oneFile_shouldReturnOne() {
        versioning.init();
        var file = writeFileToTestRepo("testFile", "content");
        var stagedFiles = versioning.stageFiles(Collections.singletonList(file));
        Assertions.assertThat(stagedFiles).isEqualTo(1);
    }

    @Test
    public void testStageFiles_threeFiles_shouldReturnThree() {
        versioning.init();
        var files = List.of(
                writeFileToTestRepo("testFile1", "content1"),
                writeFileToTestRepo("testFile2", "content2"),
                writeFileToTestRepo("testFile3", "content3")
        );
        var stagedFiles = versioning.stageFiles(files);
        Assertions.assertThat(stagedFiles).isEqualTo(3);
    }

    @Test
    public void testStageFiles_oneExistingFile_oneNonExistingFile_shouldReturnOne() {
        versioning.init();
        var files = List.of(
                writeFileToTestRepo("testFile1", "content1"),
                resolveRepositoryPath("nonExistingFile")
        );
        var stagedFiles = versioning.stageFiles(files);
        Assertions.assertThat(stagedFiles).isEqualTo(1);
    }

    @Test
    public void testStageFiles_fileOutsideRepository_shouldThrowRepositoryAccessException() {
        versioning.init();
        var file = writeFileToTestRepo("../testFile", "content");
        Assertions.assertThatThrownBy(() -> versioning.stageFiles(Collections.singletonList(file)))
                .isInstanceOf(RepositoryAccessException.class);
    }

    @Test
    public void testStageFiles_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        Assertions.assertThatThrownBy(() -> versioning.stageFiles(Collections.emptyList()))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testStageAll_noFiles_shouldReturnZero() {
        versioning.init();
        var stagedFiles = versioning.stageAll();
        Assertions.assertThat(stagedFiles).isEqualTo(0);
    }

    @Test
    public void testStageAll_oneFile_shouldReturnOne() {
        versioning.init();
        writeFileToTestRepo("testFile", "content");
        var stagedFiles = versioning.stageAll();
        Assertions.assertThat(stagedFiles).isEqualTo(1);
    }

    @Test
    public void testStageAll_threeFiles_shouldReturnThree() {
        versioning.init();
        writeFileToTestRepo("testFile1", "content1");
        writeFileToTestRepo("testFile2", "content2");
        writeFileToTestRepo("testFile3", "content3");
        var stagedFiles = versioning.stageAll();
        Assertions.assertThat(stagedFiles).isEqualTo(3);
    }

    @Test
    public void testStageAll_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        Assertions.assertThatThrownBy(() -> versioning.stageAll())
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCommit_shouldReturnCommitHash() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        Assertions.assertThat(commitHash)
                .isNotBlank()
                .hasSize(40);
    }

    @Test
    public void testCommit_twoFiles_commitShouldContainBoth() {
        var content1 = "content1";
        var content2 = "content2";

        versioning.init();
        versioning.stageFiles(List.of(
                writeFileToTestRepo("testFile1", content1),
                writeFileToTestRepo("testFile2", content2)
        ));
        versioning.commit("Test commit", true);
        Assertions.assertThat(listFilesInHeadCommit())
                .hasSize(2)
                .containsExactlyInAnyOrder(content1, content2);
    }

    @Test
    public void testCommit_twoCommits_newestCommitWithoutStagedFiles_newestCommitShouldContainFilesFromPreviousCommit() {
        var content1 = "content1";
        var content2 = "content2";

        versioning.init();
        versioning.stageFiles(List.of(
                writeFileToTestRepo("testFile1", content1),
                writeFileToTestRepo("testFile2", content2)
        ));
        versioning.commit("Test commit", true);
        versioning.commit("Test commit 2", true);
        Assertions.assertThat(listFilesInHeadCommit())
                .hasSize(2)
                .containsExactlyInAnyOrder(content1, content2);
    }

    @Test
    public void testCommit_twoCommits_newestCommitShouldContainFilesFromBoth() {
        var content1 = "content1";
        var content2 = "content2";

        versioning.init();
        versioning.stageFiles(List.of(
                writeFileToTestRepo("testFile1", content1),
                writeFileToTestRepo("testFile2", content2)
        ));
        versioning.commit("Test commit", true);

        var content3 = "content3";
        var content4 = "content4";
        versioning.stageFiles(List.of(
                writeFileToTestRepo("testFile3", content3),
                writeFileToTestRepo("testFile4", content4)
        ));
        versioning.commit("Test commit", false);
        Assertions.assertThat(listFilesInHeadCommit())
                .hasSize(4)
                .containsExactlyInAnyOrder(content1, content2, content3, content4);
    }

    @Test
    public void testCommit_blankMessage_shouldThrowIllegalArgumentException() {
        versioning.init();
        Assertions.assertThatThrownBy(() -> versioning.commit("   ", true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCommit_stagingModifiedFiles_commitShouldContainModifiedFiles() {
        var testPath = "testFile";
        var originalContent = "testContent";
        var modifiedContent = "modifiedContent";

        versioning.init();
        versioning.stageFiles(Collections.singletonList(
                writeFileToTestRepo(testPath, originalContent)
        ));
        versioning.commit("Test commit", false);

        Assertions.assertThat(listFilesInHeadCommit())
                .singleElement()
                .isEqualTo(originalContent);

        writeFileToTestRepo(testPath, modifiedContent);
        versioning.commit("Modified commit", true);

        Assertions.assertThat(listFilesInHeadCommit())
                .singleElement()
                .isEqualTo(modifiedContent);
    }

    @Test
    public void testCommit_notStagingModifiedFiles_commitShouldNotContainModifiedFiles() {
        var testPath = "testFile";
        var originalContent = "testContent";
        var modifiedContent = "modifiedContent";

        versioning.init();
        versioning.stageFiles(Collections.singletonList(
                writeFileToTestRepo(testPath, originalContent)
        ));
        versioning.commit("Test commit", false);

        Assertions.assertThat(listFilesInHeadCommit())
                .singleElement()
                .isEqualTo(originalContent);

        writeFileToTestRepo(testPath, modifiedContent);
        versioning.commit("Modified commit", false);

        Assertions.assertThat(listFilesInHeadCommit())
                .singleElement()
                .isEqualTo(originalContent);
    }

    @Test
    public void testCommit_commitOnMainBranch_shouldMoveMainBranch() {
        versioning.init();
        var commitHash = versioning.commit(REF_MAIN, "Test commit", true);
        Assertions.assertThat(versioning.listVersions(REF_MAIN, false)).containsExactly(commitHash);
    }

    @Test
    public void testCommit_commitOnHEAD_mainAndHeadEqual_shouldMoveMainBranch() {
        versioning.init();
        var commitHash = versioning.commit(REF_HEAD, "Test commit", true);
        Assertions.assertThat(versioning.listVersions(REF_MAIN, false)).containsExactly(commitHash);
    }

    @Test
    public void testCommit_commitOnMainBranch_mainAndHeadEqual_shouldMoveHEAD() {
        // Note that this is not default git behavior, but makes things easier for us
        versioning.init();
        var commitHash = versioning.commit(REF_MAIN, "Test commit", true);
        Assertions.assertThat(versioning.listVersions(REF_HEAD, false)).containsExactly(commitHash);
    }

    @Test
    public void testCommit_commitOnHead_commitOnMain_shouldResultInEqualMainAndHead() {
        versioning.init();
        var commitHash = versioning.commit(REF_HEAD, "Test commit", true);
        var commitHashOnMain = versioning.commit(REF_MAIN, "Test commit", true);
        Assertions.assertThat(versioning.listVersions(REF_MAIN, false)).containsExactly(commitHashOnMain, commitHash);
        Assertions.assertThat(versioning.listVersions(REF_HEAD, false)).containsExactly(commitHashOnMain, commitHash);
    }

    @Test
    public void testCommit_updateDifferentRefs_shouldUseDifferentParentCommits() throws Exception {
        versioning.init();

        try (var testRepository = new TestRepository<>(repository)) {
            var firstBranch = "firstBranch";
            var secondBranch = "secondBranch";

            var testRepoCommitOnFirstBranch = testRepository.branch(firstBranch).commit().message("Test commit 1").create();
            var testRepoCommitOnSecondBranch = testRepository.branch(secondBranch).commit().message("Test commit 2").create();

            var customCommitOnFirstBranch = versioning.commit(firstBranch, "Test commit 1", true);
            var customCommitOnSecondBranch = versioning.commit(secondBranch, "Test commit 2", true);

            Assertions.assertThat(versioning.listVersions(firstBranch, false))
                    .containsExactly(customCommitOnFirstBranch, testRepoCommitOnFirstBranch.getName());

            Assertions.assertThat(versioning.listVersions(secondBranch, false))
                    .containsExactly(customCommitOnSecondBranch, testRepoCommitOnSecondBranch.getName());
        }
    }

    @Test
    public void testCommit_noStagedFiles_shouldSucceed() {
        versioning.init();
        Assertions.assertThatCode(() -> versioning.commit("Test commit", false)).doesNotThrowAnyException();
    }

    @Test
    public void testCommit_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        Assertions.assertThatThrownBy(() -> versioning.commit("Test commit", true))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testGetCurrentVersionId_noCommits_shouldReturnEmptyOptional() {
        versioning.init();
        Assertions.assertThat(versioning.getCurrentVersionId()).isEmpty();
    }

    @Test
    public void testGetCurrentVersionId_oneCommit_shouldReturnOptionalWithCommitHash() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash);
    }

    @Test
    public void testGetCurrentVersionId_repositoryNotInitialized_shouldReturnEmptyOptional() {
        Assertions.assertThat(versioning.getCurrentVersionId()).isEmpty();
    }

    @Test
    public void testListVersions_noCommits_shouldReturnEmptyList() {
        versioning.init();
        Assertions.assertThat(versioning.listVersions()).isEmpty();
    }

    @Test
    public void testListVersions_oneCommit_shouldReturnCommitHash() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        Assertions.assertThat(versioning.listVersions()).containsExactly(commitHash);
    }

    @Test
    public void testListVersions_threeCommits_shouldReturnCommitHashesInDescendingOrder() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);
        Assertions.assertThat(versioning.listVersions())
                .containsExactly(commitHash3, commitHash2, commitHash1);
    }

    @Test
    public void testListVersions_descending_threeCommits_shouldReturnCommitHashesInDescendingOrder() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);
        Assertions.assertThat(versioning.listVersions(false))
                .containsExactly(commitHash3, commitHash2, commitHash1);
    }

    @Test
    public void testListVersions_ascending_threeCommits_shouldReturnCommitHashesInAscendingOrder() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);
        Assertions.assertThat(versioning.listVersions(true))
                .containsExactly(commitHash1, commitHash2, commitHash3);
    }

    @Test
    public void testListVersions_listOnDifferentBranches_shouldReturnCommitsFromSpecifiedBranch() throws Exception {
        var otherBranch = "testBranch";

        try(var testRepository = new TestRepository<>(repository)) {
            versioning.init();
            var initialCommitHash = versioning.commit("Initial commit", true);
            var initialCommit = repository.parseCommit(repository.resolve(initialCommitHash));
            var commitOnOtherBranch = testRepository.branch(otherBranch).commit().parent(initialCommit).message("Test commit 2").create();

            // check commits on main
            Assertions.assertThat(versioning.listVersions()).containsExactly(initialCommit.getName());
            // check commits on other branch
            Assertions.assertThat(versioning.listVersions(otherBranch, false)).containsExactly(
                    commitOnOtherBranch.getName(),
                    initialCommit.getName()
            );
        }
    }

    @Test
    public void testListVersions_interjectCommitOnOtherBranch_shouldReturnCommitsFromSpecifiedBranch() throws Exception {
        var otherBranch = "testBranch";

        try (var testRepository = new TestRepository<>(repository)) {
            versioning.init();
            var initialCommitOnMain =  resolveCommitHash(versioning.commit("Initial commit", true));
            var interjectedCommitOnOtherBranch = testRepository.branch(otherBranch).commit().parent(initialCommitOnMain).message("Test commit 2").create();
            var secondCommitOnMain = resolveCommitHash(versioning.commit("Test commit 1", true));

            // check commits on main
            Assertions.assertThat(versioning.listVersions()).containsExactly(secondCommitOnMain.getName(), initialCommitOnMain.getName());

            // check commits on other branch
            Assertions.assertThat(versioning.listVersions(otherBranch, false)).containsExactly(
                    interjectedCommitOnOtherBranch.getName(),
                    initialCommitOnMain.getName()
            );
        }
    }

    @Test
    public void testListVersions_repositoryNotInitialized_shouldReturnEmptyList() {
        Assertions.assertThat(versioning.listVersions()).isEmpty();
    }

    @Test
    public void testCompareVersions_sameVersion_shouldReturnEmptyList() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        Assertions.assertThat(versioning.compareVersions(commitHash, commitHash, false, null)).isEmpty();
    }

    @Test
    public void testCompareVersions_noDifferencesBetweenVersions_shouldReturnEmptyList() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        Assertions.assertThat(versioning.compareVersions(commitHash1, commitHash2, false, null)).isEmpty();
    }

    @Test
    public void testCompareVersions_addedFile_shouldReturnListWithAddDiffEntry() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        versioning.stageFiles(Collections.singletonList(writeFileToTestRepo("testFile", "content")));
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, false, null);
        Assertions.assertThat(diff).singleElement().isInstanceOf(ManagedDiffEntry.Add.class);
    }

    @Test
    public void testCompareVersions_deletedFile_shouldReturnListWithDeleteDiffEntry() {
        var filePath = "testFile";

        versioning.init();
        versioning.stageFiles(Collections.singletonList(writeFileToTestRepo(filePath, "content")));
        var commitHash1 = versioning.commit("Test commit 1", true);
        deleteFileFromTestRepo(filePath);
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, false, null);
        Assertions.assertThat(diff).singleElement().isInstanceOf(ManagedDiffEntry.Delete.class);
    }

    @Test
    public void testCompareVersions_modifyFile_shouldReturnListWithModifyDiffEntry() {
        var filePath = "testFile";
        var originalContent = "content";
        var modifiedContent = "modifiedContent";

        versioning.init();
        versioning.stageFiles(Collections.singletonList(writeFileToTestRepo(filePath, originalContent)));
        var commitHash1 = versioning.commit("Test commit 1", true);
        writeFileToTestRepo(filePath, modifiedContent);
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, true, null);
        Assertions.assertThat(diff).singleElement().isInstanceOf(ManagedDiffEntry.Modify.class);
    }

    @Test
    public void testCompareVersions_unchangedFile_includeUnchangedFalse_shouldReturnEmptyList() {
        var filePath = "testFile";
        var content = "content";

        versioning.init();
        versioning.stageFiles(Collections.singletonList(writeFileToTestRepo(filePath, content)));
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, false, null);
        Assertions.assertThat(diff).isEmpty();
    }

    @Test
    public void testCompareVersions_unchangedFile_includeUnchangedTrue_shouldReturnListWithUnchangedDiffEntry() {
        var filePath = "testFile";
        var content = "content";

        versioning.init();
        versioning.stageFiles(Collections.singletonList(writeFileToTestRepo(filePath, content)));
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, true, null);
        Assertions.assertThat(diff).singleElement().isInstanceOf(ManagedDiffEntry.Unchanged.class);
    }

    @Test
    public void testCompareVersions_differentChanges_includeUnchangedFalse_shouldReturnCorrectDiffEntries() {
        var unchangedFilePath = "unchangedFile";
        var deleteFilePath = "deleteFile";
        var modifyFilePath = "modifyFile";
        var addFilePath = "addFile";

        versioning.init();
        versioning.stageFiles(List.of(
                writeFileToTestRepo(unchangedFilePath, "unchanged"),
                writeFileToTestRepo(deleteFilePath, "delete"),
                writeFileToTestRepo(modifyFilePath, "modify")
        ));
        var commitHash1 = versioning.commit("Test commit 1", true);

        versioning.stageFiles(List.of(
                writeFileToTestRepo(addFilePath, "add"),
                writeFileToTestRepo(modifyFilePath, "modified"),
                deleteFileFromTestRepo(deleteFilePath)
        ));
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, false, null);
        Assertions.assertThat(diff).hasSize(3)
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Add.class))
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Delete.class))
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Modify.class));
    }

    @Test
    public void testCompareVersions_differentChanges_includeUnchangedTrue_shouldReturnCorrectDiffEntries() {
        var unchangedFilePath = "unchangedFile";
        var deleteFilePath = "deleteFile";
        var modifyFilePath = "modifyFile";
        var addFilePath = "addFile";

        versioning.init();
        versioning.stageFiles(List.of(
                writeFileToTestRepo(unchangedFilePath, "unchanged"),
                writeFileToTestRepo(deleteFilePath, "delete"),
                writeFileToTestRepo(modifyFilePath, "modify")
        ));
        var commitHash1 = versioning.commit("Test commit 1", true);

        versioning.stageFiles(List.of(
                writeFileToTestRepo(addFilePath, "add"),
                writeFileToTestRepo(modifyFilePath, "modified"),
                deleteFileFromTestRepo(deleteFilePath)
        ));
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, true, null);
        Assertions.assertThat(diff).hasSize(4)
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Unchanged.class))
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Add.class))
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Delete.class))
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Modify.class));
    }

    @Test
    public void testCompareVersions_differentChanges_withPreprocessingFunction_shouldReturnCorrectDiffEntries() {
        var deleteFilePath = "deleteFile";
        var modifyFilePath = "modifyFile";
        var addFilePath = "addFile";

        versioning.init();
        versioning.stageFiles(List.of(
                writeFileToTestRepo(deleteFilePath, "delete"),
                writeFileToTestRepo(modifyFilePath, "modify")
        ));
        var commitHash1 = versioning.commit("Test commit 1", true);

        versioning.stageFiles(List.of(
                writeFileToTestRepo(addFilePath, "add"),
                writeFileToTestRepo(modifyFilePath, "modified"),
                deleteFileFromTestRepo(deleteFilePath)
        ));
        var commitHash2 = versioning.commit("Test commit 2", true);

        var diff = versioning.compareVersions(commitHash1, commitHash2, true, ManagedRepositoryObject::getRawFileContent);
        Assertions.assertThat(diff).hasSize(3)
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Add.class))
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Delete.class))
                .anySatisfy(d -> Assertions.assertThat(d).isInstanceOf(ManagedDiffEntry.Modify.class));
    }

    @Test
    public void testCompareVersions_oldVersionNotExisting_shouldThrowRepositoryVersioningException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var nonExistingCommitHash = commitHash + "123";
        Assertions.assertThatThrownBy(() -> versioning.compareVersions(nonExistingCommitHash, commitHash, false, null))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCompareVersions_newVersionNotExisting_shouldThrowRepositoryVersioningException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var nonExistingCommitHash = commitHash + "123";
        Assertions.assertThatThrownBy(() -> versioning.compareVersions(commitHash, nonExistingCommitHash, false, null))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCompareVersions_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        var commitHash = "commitHash";
        Assertions.assertThatThrownBy(() -> versioning.compareVersions(commitHash, commitHash, false, null))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCompareVersions_compareBasedOnTags_shouldReturnCorrectDiffEntries() {
        var tagName1 = "tag1";
        var tagName2 = "tag2";

        versioning.init();
        var commit1 = versioning.commit("Test commit 1", true);

        writeFileToTestRepo("testFile", "content");
        versioning.stageAll();
        var commit2 = versioning.commit("Test commit 2", true);

        versioning.tagCommit(commit1, tagName1);
        versioning.tagCommit(commit2, tagName2);

        var diff = versioning.compareVersions(tagName1, tagName2, false, null);
        Assertions.assertThat(diff).hasSize(1);
    }

    @Test
    public void testCheckout_checkoutExistingVersion_shouldCheckoutVersion() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit", true);
        var commitHash2 = versioning.commit("Test commit 2", true);

        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash2);
        versioning.checkout(commitHash1);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash1);
    }

    @Test
    public void testCheckout_checkoutCurrentVersionId_shouldNotThrowException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var versionBeforeCheckout = versioning.getCurrentVersionId();
        Assertions.assertThatCode(() -> versioning.checkout(commitHash)).doesNotThrowAnyException();
        var versionAfterCheckout = versioning.getCurrentVersionId();
        Assertions.assertThat(versionBeforeCheckout).isEqualTo(versionAfterCheckout);
    }

    @Test
    public void testCheckout_checkoutNonExistingVersion_shouldThrowRepositoryVersioningException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var nonExistingCommitHash = commitHash + "123";
        Assertions.assertThatThrownBy(() -> versioning.checkout(nonExistingCommitHash))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCheckout_threeVersions_shouldBeAbleToCheckoutEachVersion() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);

        versioning.checkout(commitHash1);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash1);

        versioning.checkout(commitHash2);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash2);

        versioning.checkout(commitHash3);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash3);
    }

    @Test
    public void testCheckout_filesInWorkingTree_shouldStillBeAbleToCheckout() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);

        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash2);
        writeFileToTestRepo("testFile", "content");
        Assertions.assertThatCode(() -> versioning.checkout(commitHash1)).doesNotThrowAnyException();
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash1);
    }

    @Test
    public void testCheckout_multipleVersions_versionsListShouldStillContainAllVersions() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);

        versioning.checkout(commitHash1);

        Assertions.assertThat(versioning.listVersions()).containsExactly(commitHash3, commitHash2, commitHash1);
    }

    @Test
    public void testCheckout_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        var commitHash = "commitHash";
        Assertions.assertThatThrownBy(() -> versioning.checkout(commitHash))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testCheckout_commitAfterCheckout_shouldMoveMainButNotHead() {
        // not default git behavior, but intended
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        versioning.checkout(commitHash1);
        var commitHash3 = versioning.commit("Test commit 3", true);

        Assertions.assertThat(versioning.listVersions()).containsExactly(commitHash3, commitHash2, commitHash1);
        Assertions.assertThat(versioning.listVersions(REF_HEAD, false)).containsExactly(commitHash1);
    }

    @Test
    public void testCheckout_checkoutOldBranch_thenCheckoutMainAgain_futureCommitsShouldMoveBothHEADAndMain() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        versioning.checkout(commitHash1);

        Assertions.assertThat(versioning.listVersions(REF_HEAD, false)).containsExactly(commitHash1);
        Assertions.assertThat(versioning.listVersions(REF_MAIN, false)).containsExactly(commitHash2, commitHash1);

        versioning.checkout(commitHash2);

        Assertions.assertThat(versioning.listVersions(REF_HEAD, false)).containsExactly(commitHash2, commitHash1);
        Assertions.assertThat(versioning.listVersions(REF_MAIN, false)).containsExactly(commitHash2, commitHash1);

        var commitHash3 = versioning.commit("Test commit 3", true);

        Assertions.assertThat(versioning.listVersions(REF_HEAD, false)).containsExactly(commitHash3, commitHash2, commitHash1);
        Assertions.assertThat(versioning.listVersions(REF_MAIN, false)).containsExactly(commitHash3, commitHash2, commitHash1);
    }

    @Test
    public void testCheckout_multipleBranches_choosesPreferredBranch() throws Exception {
        var branch1 = "branch1";
        var branch2 = "branch2";

        versioning.init();
        try (var testRepository = new TestRepository<>(repository)) {
            var commit = testRepository.commit().message("Test commit").create();
            testRepository.branch("branch1").update(commit);
            testRepository.branch("branch2").update(commit);

            versioning.checkout(commit.getName(), true, branch1);
            Assertions.assertThat(repository.getBranch()).isEqualTo(branch1);

            versioning.checkout(commit.getName(), true, branch2);
            Assertions.assertThat(repository.getBranch()).isEqualTo(branch2);
        }
    }

    @Test
    public void testCheckout_checkoutCommitWithBranch_attachToBranchTrue_shouldCheckoutBranch() throws Exception {
        var branch = "branch";

        versioning.init();
        try (var testRepository = new TestRepository<>(repository)) {
            var commit = testRepository.commit().message("Test commit").create();
            testRepository.branch(branch).update(commit);

            versioning.checkout(commit.getName(), true, null);
            Assertions.assertThat(repository.getBranch()).isEqualTo(branch);
        }
    }

    @Test
    public void testCheckout_checkoutCommitWithBranch_attachToBranchFalse_shouldDetachHead() throws Exception {
        var branch = "branch";

        versioning.init();
        try (var testRepository = new TestRepository<>(repository)) {
            var commit = testRepository.commit().message("Test commit").create();
            testRepository.branch(branch).update(commit);

            versioning.checkout(commit.getName(), false, null);
            Assertions.assertThat(repository.getBranch())
                    .isNotEqualTo(branch)
                    .hasSize(40);
        }
    }

    @Test
    public void testCheckout_checkoutCommitWithBranch_attachToBranchDefault_shouldCheckoutBranch() throws Exception {
        var branch = "branch";

        versioning.init();
        try (var testRepository = new TestRepository<>(repository)) {
            var commit = testRepository.commit().message("Test commit").create();
            testRepository.branch(branch).update(commit);

            versioning.checkout(commit.getName());
            Assertions.assertThat(repository.getBranch()).isEqualTo(branch);
        }
    }

    @Test
    public void testCheckout_checkoutCommitWithoutBranch_attachToBranchTrue_shouldDetachHead() throws Exception {
        versioning.init();
        try (var testRepository = new TestRepository<>(repository)) {
            var commit = testRepository.commit().message("Test commit").create();

            versioning.checkout(commit.getName(), true, null);
            Assertions.assertThat(repository.getBranch())
                    .isNotEqualTo(REF_HEAD)
                    .hasSize(40);
        }
    }

    @Test
    public void testCheckout_checkoutBasedOnTag_shouldCheckoutCorrectVersion() {
        var tagName = "testTag";

        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        versioning.tagCommit(commitHash, tagName);
        versioning.checkout(tagName);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash);
    }

    @Test
    public void testReset_resetToExistingVersion_shouldResetToVersion() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);

        versioning.checkout(commitHash1);
        versioning.reset(commitHash2);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash2);
    }

    @Test
    public void testReset_resetToCurrentVersionId_shouldNotThrowException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var versionBeforeReset = versioning.getCurrentVersionId();
        Assertions.assertThatCode(() -> versioning.reset(commitHash)).doesNotThrowAnyException();
        var versionAfterReset = versioning.getCurrentVersionId();
        Assertions.assertThat(versionBeforeReset).isEqualTo(versionAfterReset);
    }

    @Test
    public void testReset_resetToNonExistingVersion_shouldThrowRepositoryVersioningException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var nonExistingCommitHash = commitHash + "123";
        Assertions.assertThatThrownBy(() -> versioning.reset(nonExistingCommitHash))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testReset_threeVersions_shouldBeAbleToResetToEachVersion() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);

        versioning.checkout(commitHash1);
        versioning.reset(commitHash2);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash2);

        versioning.reset(commitHash3);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash3);
    }

    @Test
    public void testReset_stagedFiles_fileContentIsReset() {
        var stagedFilePath = "stagedFile";
        var stagedFileContent = "stagedContent";
        var stagedFileModifiedContent = "stagedModifiedContent";

        versioning.init();

        // stage and commit original file
        var stagedFile = writeFileToTestRepo(stagedFilePath, stagedFileContent);
        versioning.stageFiles(Collections.singletonList(stagedFile));
        var commitHash = versioning.commit("Test commit 1", true);
        Assertions.assertThat(stagedFile).exists().hasContent(stagedFileContent);

        // modify file content and stage again
        versioning.stageFiles(Collections.singletonList(writeFileToTestRepo(stagedFilePath, stagedFileModifiedContent)));
        Assertions.assertThat(stagedFile).exists().hasContent(stagedFileModifiedContent);

        // check that reset reverts staged changes to content of previous commit
        versioning.reset(commitHash);
        Assertions.assertThat(stagedFile).exists().hasContent(stagedFileContent);
    }

    @Test
    public void testReset_multipleVersions_shouldNotContainVersionsAfterResetCommit() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);

        Assertions.assertThat(versioning.listVersions()).containsExactly(commitHash3, commitHash2, commitHash1);
        versioning.reset(commitHash1);
        Assertions.assertThat(versioning.listVersions()).containsExactly(commitHash1);
    }

    @Test
    public void testReset_resetBasedOnTag_shouldResetToCorrectVersion() {
        var tagName = "testTag";

        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        versioning.commit("Test commit 2", true);
        versioning.tagCommit(commitHash1, tagName);

        versioning.reset(tagName);
        Assertions.assertThat(versioning.getCurrentVersionId()).contains(commitHash1);
    }

    @Test
    public void testTagCommit_tagExistingCommit_shouldTagCommit() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var tagName = "testTag";
        versioning.tagCommit(commitHash, tagName);
        Assertions.assertThat(versioning.listTagsForCommit(commitHash)).containsExactly(tagName);
    }

    @Test
    public void testTagCommit_tagNonExistingCommit_shouldThrowRepositoryVersioningException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var nonExistingCommitHash = commitHash + "123";
        var tagName = "testTag";
        Assertions.assertThatThrownBy(() -> versioning.tagCommit(nonExistingCommitHash, tagName))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testTagCommit_tagExistingCommitWithExistingTag_shouldThrowRepositoryVersioningException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var tagName = "testTag";
        versioning.tagCommit(commitHash, tagName);
        Assertions.assertThatThrownBy(() -> versioning.tagCommit(commitHash, tagName))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testTagCommit_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        var commitHash = "commitHash";
        var tagName = "testTag";
        Assertions.assertThatThrownBy(() -> versioning.tagCommit(commitHash, tagName))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testListTagsForCommit_commitWithNoTags_shouldReturnEmptyList() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        Assertions.assertThat(versioning.listTagsForCommit(commitHash)).isEmpty();
    }

    @Test
    public void testListTagsForCommit_commitWithOneTag_shouldReturnListWithOneTag() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var tagName = "testTag";
        versioning.tagCommit(commitHash, tagName);
        Assertions.assertThat(versioning.listTagsForCommit(commitHash)).containsExactly(tagName);
    }

    @Test
    public void testListTagsForCommit_commitWithMultipleTags_shouldReturnListWithAllTags() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var tagNames = List.of("testTag1", "testTag2", "testTag3");
        tagNames.forEach(tagName -> versioning.tagCommit(commitHash, tagName));
        Assertions.assertThat(versioning.listTagsForCommit(commitHash)).containsExactlyElementsOf(tagNames);
    }

    @Test
    public void testListTagsForCommit_nonExistingCommit_shouldThrowRepositoryVersioningException() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var nonExistingCommitHash = commitHash + "123";
        Assertions.assertThatThrownBy(() -> versioning.listTagsForCommit(nonExistingCommitHash))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testListTagsForCommit_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        var commitHash = "commitHash";
        Assertions.assertThatThrownBy(() -> versioning.listTagsForCommit(commitHash))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @Test
    public void testListTagsForCommit_twoCommitsWithOneTagsEach_shouldAlwaysReturnCorrectTags() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var tag1 = "testTag1";
        var tag2 = "testTag2";
        versioning.tagCommit(commitHash1, tag1);
        versioning.tagCommit(commitHash2, tag2);

        Assertions.assertThat(versioning.listTagsForCommit(commitHash1)).containsExactly(tag1);
        Assertions.assertThat(versioning.listTagsForCommit(commitHash2)).containsExactly(tag2);
    }

    @Test
    public void testListTagsForCommit_multipleTagsOnDifferentCommits_shouldAlwaysReturnCorrectTags() {
        var tag1 = "testTag1";
        var tags3 = List.of("testTag2", "testTag3");
        var tag4 = "testTag4";
        var tags5 = List.of("testTag5", "testTag6", "testTag7", "testTag8", "testTag9");

        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);
        var commitHash4 = versioning.commit("Test commit 4", true);
        var commitHash5 = versioning.commit("Test commit 5", true);
        versioning.tagCommit(commitHash1, tag1);
        tags3.forEach(tag -> versioning.tagCommit(commitHash3, tag));
        versioning.tagCommit(commitHash4, tag4);
        tags5.forEach(tag -> versioning.tagCommit(commitHash5, tag));

        Assertions.assertThat(versioning.listTagsForCommit(commitHash1)).containsExactly(tag1);
        Assertions.assertThat(versioning.listTagsForCommit(commitHash2)).isEmpty();
        Assertions.assertThat(versioning.listTagsForCommit(commitHash3)).containsExactlyElementsOf(tags3);
        Assertions.assertThat(versioning.listTagsForCommit(commitHash4)).containsExactly(tag4);
        Assertions.assertThat(versioning.listTagsForCommit(commitHash5)).containsExactlyElementsOf(tags5);
    }

    @Test
    public void testListTags_noTags_shouldReturnEmptyList() {
        versioning.init();
        Assertions.assertThat(versioning.listTags()).isEmpty();
    }

    @Test
    public void testListTags_oneTag_shouldReturnListWithOneTag() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var tagName = "testTag";
        versioning.tagCommit(commitHash, tagName);
        Assertions.assertThat(versioning.listTags()).containsExactly(tagName);
    }

    @Test
    public void testListTags_multipleTags_shouldReturnListWithAllTags() {
        versioning.init();
        var commitHash = versioning.commit("Test commit", true);
        var tagNames = List.of("testTag1", "testTag2", "testTag3");
        tagNames.forEach(tagName -> versioning.tagCommit(commitHash, tagName));
        Assertions.assertThat(versioning.listTags()).containsExactlyElementsOf(tagNames);
    }

    @Test
    public void testListTags_repositoryWithMultipleCommitsAndTags_shouldReturnListWithAllTags() {
        versioning.init();
        var commitHash1 = versioning.commit("Test commit 1", true);
        var commitHash2 = versioning.commit("Test commit 2", true);
        var commitHash3 = versioning.commit("Test commit 3", true);
        var tag1 = "testTag1";
        var tag2 = "testTag2";
        var tag3 = "testTag3";
        versioning.tagCommit(commitHash1, tag1);
        versioning.tagCommit(commitHash2, tag2);
        versioning.tagCommit(commitHash3, tag3);
        Assertions.assertThat(versioning.listTags()).containsExactlyInAnyOrder(tag1, tag2, tag3);
    }

    @Test
    public void testListTags_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        Assertions.assertThatThrownBy(() -> versioning.listTags())
                .isInstanceOf(RepositoryVersioningException.class);
    }

    @SneakyThrows(IOException.class)
    private Repository getJGitRepository() {
        return new FileRepositoryBuilder()
                .setWorkTree(tempDir.resolve(testRepo).toFile())
                .build();
    }

    @Test
    public void testReset_repositoryNotInitialized_shouldThrowRepositoryVersioningException() {
        var commitHash = "commitHash";
        Assertions.assertThatThrownBy(() -> versioning.reset(commitHash))
                .isInstanceOf(RepositoryVersioningException.class);
    }

    private ManagedRepositoryVersioning getManagedRepositoryVersioning(Repository repository) {
        return new ManagedRepositoryVersioning(repository, testRepo, encoding, getWorkDir());
    }

    private boolean repositoryExists(Repository repository) {
        return repository.getObjectDatabase() != null && repository.getObjectDatabase().exists();
    }

    @SneakyThrows(IOException.class)
    private Path writeFileToTestRepo(String path, String content) {
        var fullPath = resolveRepositoryPath(path);
        Files.createDirectories(fullPath.getParent());
        return Files.writeString(fullPath, content, encoding);
    }

    @SneakyThrows(IOException.class)
    private Path deleteFileFromTestRepo(String path) {
        var fullPath = resolveRepositoryPath(path);
        Files.delete(fullPath);
        return fullPath;
    }

    private Path resolveRepositoryPath(String path) {
        return getWorkDir().resolve(path);
    }

    private Path getWorkDir() {
        return repository.getWorkTree().toPath();
    }

    @SneakyThrows(IOException.class)
    private RevCommit resolveCommitHash(String hash) {
        return repository.parseCommit(repository.resolve(hash));
    }

    @SneakyThrows(IOException.class)
    private List<String> listFilesInHeadCommit() {
        var headCommit = repository.parseCommit(repository.resolve(REF_HEAD));
        return listFilesInCommit(headCommit);
    }

    @SneakyThrows(IOException.class)
    private List<String> listFilesInCommit(RevCommit commit) {
        try (TreeWalk tw = new TreeWalk(repository)) {
            tw.addTree(commit.getTree());
            tw.setRecursive(true);
            var files = new ArrayList<String>();
            while (tw.next()) {
                var objReader = tw.getObjectReader();
                var content = new String(objReader.open(tw.getObjectId(0)).getBytes(), encoding);
                files.add(content);
            }
            return files;
        }
    }
}
