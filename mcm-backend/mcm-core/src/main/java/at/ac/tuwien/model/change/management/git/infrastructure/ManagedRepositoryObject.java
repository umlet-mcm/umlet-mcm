package at.ac.tuwien.model.change.management.git.infrastructure;

import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import lombok.Getter;
import lombok.NonNull;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.RawParseUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class ManagedRepositoryObject {

    private final ObjectId objectId;

    private final Repository repository;

    @Getter
    private final Charset encoding;

    private final boolean cacheFileContent;
    private byte[] rawFilePath = null;
    private String filePath = null;
    private byte[] rawFileContent = null;
    private String fileContent = null;

    ManagedRepositoryObject(
            @NonNull ObjectId objectId,
            @NonNull String filePath,
            @NonNull Repository repository,
            @NonNull Charset encoding,
            boolean cacheFileContent) {
        this.objectId = objectId;
        this.filePath = filePath;
        this.repository = repository;
        this.encoding = encoding;
        this.cacheFileContent = cacheFileContent;
    }


    ManagedRepositoryObject(@NonNull TreeWalk treeWalk, @NonNull Repository repository, @NonNull Charset encoding) {
        this.rawFilePath = treeWalk.getRawPath();
        this.repository = repository;
        this.encoding = encoding;

        var id = treeWalk.getObjectId(0);
        if (id.equals(ObjectId.zeroId())) {
            throw new RepositoryReadException("Cannot read object ID for repository file: " + treeWalk.getPathString());
        }
        this.objectId = id;
        this.cacheFileContent = false;
    }

    public byte[] getRawFilePath() {
        if (rawFilePath == null) {
            if (filePath == null) {
                throw new RepositoryReadException("Both raw file path and file path set to null for object with ID: " + objectId);
            }
            rawFilePath = filePath.getBytes(encoding);
        }
        return rawFilePath;
    }

    public String getFilePath() {
        if (filePath == null) {
            if (rawFilePath == null) {
                throw new RepositoryReadException("Both raw file path and file path set to null for object with ID: " + objectId);
            }
            filePath = decode(rawFilePath, encoding);
        }
        return filePath;
    }

    public byte[] getRawFileContent() {
        if (! cacheFileContent) return computeRawFileContent();
        if (rawFileContent == null) {
            rawFileContent = computeRawFileContent();
        }
        return rawFileContent;
    }

    public String getFileContent() {
        if (! cacheFileContent) return decode(getRawFileContent(), encoding);
        if (fileContent == null) {
            fileContent = decode(getRawFileContent(), encoding);
        }
        return fileContent;
    }

    public boolean rawFilePathMatches(byte @NonNull [] otherPath) {
        return RawParseUtils.match(getRawFilePath(), 0, otherPath) != -1;
    }

    private byte[] computeRawFileContent() {
        try {
            var loader = repository.open(objectId);
            if (loader == null) {
                throw new RepositoryReadException("Failed to read object with ID: " + objectId);
            }
            return loader.getCachedBytes();
        } catch (IOException e) {
            throw new RepositoryReadException("Failed to read object with ID: " + objectId, e);
        }
    }

    private static String decode(byte[] buffer, Charset encoding) {
        return RawParseUtils.decode(encoding, buffer, 0, buffer.length);
    }
}
