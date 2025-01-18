package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryVersioningException;
import com.google.common.collect.Streams;
import lombok.NonNull;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.QuotedString;
import org.springframework.lang.Nullable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ManagedDiffFormatter implements AutoCloseable {

    private final ByteArrayOutputStream diffFmtByteOutput;
    private final OutputStream diffFmtOutput;
    private final DiffFormatter diffFormatter;
    private final DiffAlgorithm diffAlgorithm;
    private final RawTextComparator diffComparator = RawTextComparator.DEFAULT;
    private final Repository repository;
    private final Charset encoding;
    private final String repositoryName;


    ManagedDiffFormatter(
            @NonNull Repository repository,
            @NonNull Charset encoding,
            @NonNull String repositoryName
    ) {
        this.repository = repository;
        this.encoding = encoding;
        this.repositoryName = repositoryName;

        this.diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM);
        this.diffFmtByteOutput = new ByteArrayOutputStream();
        this.diffFmtOutput = new BufferedOutputStream(diffFmtByteOutput);
        this.diffFormatter = new DiffFormatter(diffFmtOutput);
        diffFormatter.setRepository(repository);
        diffFormatter.setDiffAlgorithm(diffAlgorithm);
        diffFormatter.setDiffComparator(diffComparator);
        diffFormatter.setDetectRenames(false);
        diffFormatter.setPathFilter(TreeFilter.ALL);
    }

    List<ManagedDiffEntry> createUnifiedDiff(
            @NonNull RevCommit oldCommit,
            @NonNull RevCommit newCommit,
            boolean includeUnchanged
    ) {
        return createUnifiedDiff(oldCommit, newCommit, includeUnchanged, null);
    }

    List<ManagedDiffEntry> createUnifiedDiff(
            @NonNull RevCommit oldCommit,
            @NonNull RevCommit newCommit,
            boolean includeUnchanged,
            @Nullable Function<ManagedRepositoryObject, byte[]> objectPreprocessor
    ) {
        List<DiffEntry> diffEntries = createDiffEntries(oldCommit, newCommit);
        List<ManagedRepositoryObject> unchangedObjects = includeUnchanged
                ? getUnchangedObjects(
                oldCommit,
                diffEntries.stream()
                        .map(DiffEntry::getOldId)
                        .map(AbbreviatedObjectId::toObjectId)
                        .collect(Collectors.toSet()))
                : Collections.emptyList();
        return Streams.concat(
                diffEntries.stream().map(diffEntry -> createManagedDiffEntry(diffEntry, objectPreprocessor))
                        .flatMap(Optional::stream),
                unchangedObjects.stream().map(object -> createUnchangedDiffEntry(object, objectPreprocessor))
                        .flatMap(Optional::stream)
        ).toList();
    }

    @Override
    public void close() throws IOException {
        diffFormatter.close();
        diffFmtOutput.close();
        diffFmtByteOutput.close();
    }

    private List<DiffEntry> createDiffEntries(RevCommit oldCommit, RevCommit newCommit) {
        try {
            return diffFormatter.scan(oldCommit.getTree(), newCommit.getTree());
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to create diff entries in repository when comparing versions '"
                    + oldCommit.getName() + "' and '" + newCommit.getName() + "' in repository: " + repositoryName, e);

        }
    }

    private Optional<ManagedDiffEntry> createManagedDiffEntry(
            DiffEntry diff,
            @Nullable Function<ManagedRepositoryObject, byte[]> objectPreprocessor
    ) {

        return switch (diff.getChangeType()) {
            case ADD -> {
                var newObject = new ManagedRepositoryObject(diff.getNewId().toObjectId(), diff.getNewPath(), repository, encoding, true);
                yield getUnifiedDiffOutput(diff, getEmptyText(), getObjectText(newObject, objectPreprocessor))
                        .map(diffOutput -> new ManagedDiffEntry.Add(newObject, diffOutput));
            }
            case MODIFY -> {
                var oldObject = new ManagedRepositoryObject(diff.getOldId().toObjectId(), diff.getOldPath(), repository, encoding, true);
                var newObject = new ManagedRepositoryObject(diff.getNewId().toObjectId(), diff.getNewPath(), repository, encoding, true);
                yield getUnifiedDiffOutput(diff, getObjectText(oldObject, objectPreprocessor), getObjectText(newObject, objectPreprocessor))
                        .map(diffOutput -> new ManagedDiffEntry.Modify(oldObject, newObject, diffOutput));
            }
            case DELETE -> {
                var oldObject = new ManagedRepositoryObject(diff.getOldId().toObjectId(), diff.getOldPath(), repository, encoding, true);
                yield getUnifiedDiffOutput(diff, getObjectText(oldObject, objectPreprocessor), getEmptyText())
                        .map(diffOutput -> new ManagedDiffEntry.Delete(oldObject, diffOutput));
            }
            default -> throw new RepositoryVersioningException("Unsupported diff entry type: " + diff.getChangeType());
        };
    }

    private List<ManagedRepositoryObject> getUnchangedObjects(
            RevCommit commit,
            Set<ObjectId> changedObjects
    ) {
        try (var tw = new TreeWalk(repository)) {
            tw.addTree(commit.getTree());
            tw.setRecursive(true);

            var unchangedObjects = new ArrayList<ManagedRepositoryObject>();

            while (tw.next()) {
                if (changedObjects.contains(tw.getObjectId(0))) continue;
                var managedObject = new ManagedRepositoryObject(tw, repository, encoding);
                unchangedObjects.add(managedObject);
            }
            return unchangedObjects;
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to traverse tree for '" + commit.getName()
                    + "' while creating diff entries in repository: " + repositoryName, e);
        }
    }

    private Optional<ManagedDiffEntry> createUnchangedDiffEntry(
            ManagedRepositoryObject managedObject,
            @Nullable Function<ManagedRepositoryObject, byte[]> objectPreprocessor
    ) {
        var byteText = getObjectBytes(managedObject, objectPreprocessor);
        if (byteText.length == 0) return Optional.empty();
        var text = new String(byteText, encoding);
        return Optional.of(new ManagedDiffEntry.Unchanged(managedObject, text));
    }

    private RawText getEmptyText() {
        return new RawText(new byte[0]);
    }

    private RawText getObjectText(
            ManagedRepositoryObject managedObject,
            @Nullable Function<ManagedRepositoryObject, byte[]> objectPreprocessor
    ) {
        var byteText = getObjectBytes(managedObject, objectPreprocessor);
        return new RawText(byteText);
    }

    private byte[] getObjectBytes(
            ManagedRepositoryObject managedObject,
            @Nullable Function<ManagedRepositoryObject, byte[]> objectPreprocessor
    ) {
        return Optional.ofNullable(objectPreprocessor)
                .map(preprocessor -> preprocessor.apply(managedObject))
                .orElseGet(managedObject::getRawFileContent);
    }


    private Optional<String> getUnifiedDiffOutput(DiffEntry diff, RawText oldContent, RawText newContent) {
        return getUnifiedDiffOutput(diff, oldContent, newContent, Math.max(oldContent.size(), newContent.size()));
    }

    private Optional<String> getUnifiedDiffOutput(DiffEntry diff, RawText oldContent, RawText newContent, int context) {
        try {
            var edits = diffAlgorithm.diff(diffComparator, oldContent, newContent);
            if (edits.isEmpty()) return Optional.empty();

            var header = getUnifiedDiffHeader(diff);
            diffFormatter.setContext(context);
            diffFormatter.format(edits, oldContent, newContent);
            return Optional.of(header + getDiffFormatterOutput());
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to get unified diff output", e);
        }
    }

    private String getUnifiedDiffHeader(DiffEntry diff) {
        return new UnifiedDiffHeaderBuilder(diff).build();
    }


    private String getDiffFormatterOutput() {
        try {
            diffFmtOutput.flush();
            var output = diffFmtByteOutput.toString(encoding);
            diffFmtByteOutput.reset();
            return output;
        } catch (IOException e) {
            throw new RepositoryVersioningException("Failed to get diff formatter output", e);
        }
    }

    // While JGIT does include a `formatHeader` method, it's private
    // And  we can't use their public `format` method while allowing for a preprocessor function
    // so we have to build the header ourselves
    private static class UnifiedDiffHeaderBuilder {
        private final StringBuilder headerBuilder = new StringBuilder();

        private final String oldPath;
        private final String newPath;
        private final DiffEntry.ChangeType changeType;
        private final FileMode oldMode;
        private final FileMode newMode;
        private final AbbreviatedObjectId oldId;
        private final AbbreviatedObjectId newId;
        private final int similarityScore;

        private final String oldPrefix = "a/";
        private final String newPrefix = "b/";

        UnifiedDiffHeaderBuilder(DiffEntry diff) {
            this.oldPath = diff.getOldPath() != null ? diff.getOldPath() : "/dev/null";
            this.newPath = diff.getNewPath() != null ? diff.getNewPath() : "/dev/null";

            if (diff.getChangeType() == DiffEntry.ChangeType.RENAME || diff.getChangeType() == DiffEntry.ChangeType.COPY) {
                throw new RepositoryVersioningException("Unified diff header for RENAME and COPY operations is not supported for diff entry: " + diff);
            }

            this.changeType = diff.getChangeType();
            this.oldMode = diff.getOldMode();
            this.newMode = diff.getNewMode();
            this.oldId = diff.getOldId();
            this.newId = diff.getNewId();
            this.similarityScore = diff.getScore();
        }


        String build() {
            headerBuilder.setLength(0);
            formatGitDiffFirstHeaderLine();
            formatFileModeChanges();
            formatIndexLine();
            formatFilePaths();
            return headerBuilder.toString();
        }

        private void formatGitDiffFirstHeaderLine() {
            headerBuilder.append("diff --git ");
            headerBuilder.append(quotePath(oldPrefix + (changeType == DiffEntry.ChangeType.ADD ? newPath : oldPath)));
            headerBuilder.append(' ');
            headerBuilder.append(quotePath(newPrefix + (changeType == DiffEntry.ChangeType.DELETE ? oldPath : newPath)));
            headerBuilder.append('\n');
        }

        private void formatFileModeChanges() {
            if (isModify()) {
                if (oldMode != null && newMode != null && !oldMode.equals(newMode)) {
                    headerBuilder.append("old mode ").append(formatFileMode(oldMode)).append('\n');
                    headerBuilder.append("new mode ").append(formatFileMode(newMode)).append('\n');
                }
                if (similarityScore > 0) {
                    headerBuilder.append("dissimilarity index ").append(100 - similarityScore).append("%\n");
                }
            } else if (isAdd() && newMode != null) {
                headerBuilder.append("new file mode ").append(formatFileMode(newMode)).append('\n');
            } else if (isDelete() && oldMode != null) {
                headerBuilder.append("deleted file mode ").append(formatFileMode(oldMode)).append('\n');
            }
        }

        private void formatIndexLine() {
            if (oldId != null && newId != null) {
                headerBuilder.append("index ").append(oldId.name())
                        .append("..").append(newId.name());
                if (oldMode != null && oldMode.equals(newMode)) {
                    headerBuilder.append(' ').append(formatFileMode(newMode));
                }
                headerBuilder.append('\n');
            }
        }

        private void formatFilePaths() {
            String oldFilePath = isAdd()
                    ? "/dev/null"
                    : quotePath(oldPrefix + oldPath);

            String newFilePath = isDelete()
                    ? "/dev/null"
                    : quotePath(newPrefix + newPath);

            headerBuilder.append("--- ").append(oldFilePath).append('\n');
            headerBuilder.append("+++ ").append(newFilePath).append('\n');
        }

        private String quotePath(String path) {
            return QuotedString.GIT_PATH.quote(path);
        }

        private String formatFileMode(FileMode mode) {
            if (mode == null) {
                return "";
            }
            return String.format("%06o", mode.getBits());
        }

        private boolean isAdd() {
            return changeType == DiffEntry.ChangeType.ADD;
        }

        private boolean isDelete() {
            return changeType == DiffEntry.ChangeType.DELETE;
        }

        private boolean isModify() {
            return changeType == DiffEntry.ChangeType.MODIFY;
        }
    }
}
