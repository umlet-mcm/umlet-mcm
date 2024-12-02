package at.ac.tuwien.model.change.management.git.util;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationReadException;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationWriteException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class RepositoryUtils {
    // TODO: considerations on exceptions. Do we really want to throw so early?
    private static final String DIRECTORY_NODES = "nodes";
    private static final String DIRECTORY_RELATIONS = "relations";
    private static final String DIRECTORY_MODELS = "models";
    private static final Charset FILE_ENCODING = StandardCharsets.UTF_8;


    // JGit comparisons on raw byte arrays are faster than on Strings
    private static final byte[] DIRECTORY_NODES_RAW = Constants.encode(DIRECTORY_NODES);
    private static final byte[] DIRECTORY_RELATIONS_RAW = Constants.encode(DIRECTORY_RELATIONS);
    private static final byte[] DIRECTORY_MODELS_RAW = Constants.encode(DIRECTORY_MODELS);

    private final static class Patterns {
        private static final Pattern PATHNAME_SEPARATOR = Pattern.compile("[/\\\\]");
        private static final Pattern PATHNAME_INVALID_CHARS = Pattern.compile("[^\\p{L}\\p{N} ._-]");
        private static final Pattern PATHNAME_STRIP_CHARS = Pattern.compile("^[.-]+|[.-]+$");
        private static final Pattern PATHNAME_RESERVED = Pattern.compile("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$", Pattern.CASE_INSENSITIVE);
        private static final int PATHNAME_MAX_LENGTH = 127; // Adjust as necessary
    }

    public static String sanitizeDirectoryName(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Directory name cannot be null or empty");
        }

        String sanitizedDirname = Normalizer.normalize(input, Normalizer.Form.NFKC);
        sanitizedDirname = Patterns.PATHNAME_SEPARATOR.matcher(sanitizedDirname).replaceAll("-");
        sanitizedDirname = Patterns.PATHNAME_INVALID_CHARS.matcher(sanitizedDirname).replaceAll("");
        sanitizedDirname = Patterns.PATHNAME_STRIP_CHARS.matcher(sanitizedDirname).replaceAll("");
        sanitizedDirname = sanitizedDirname.strip();
        if (Patterns.PATHNAME_RESERVED.matcher(sanitizedDirname).matches()) {
            sanitizedDirname = "_" + sanitizedDirname;
        }

        if (sanitizedDirname.length() > Patterns.PATHNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Directory name '" + input + "' cannot be longer than " + Patterns.PATHNAME_MAX_LENGTH + " characters");
        }

        if (sanitizedDirname.isBlank()) {
            throw new IllegalArgumentException("Directory name '" + input + "' contains no valid characters");
        }

        return sanitizedDirname;
    }


    public static Path writeRepositoryFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, FILE_ENCODING);
            return path;
        } catch (IOException e) {
            throw new ConfigurationWriteException("Failed to write content to file '" + path + "'", e);
        }
    }

    public static boolean repositoryExists(Repository repository) {
        return repository.getObjectDatabase() != null && repository.getObjectDatabase().exists();
    }

    public static String headCommitHash(Repository repository) {
        try {
            return repository.resolve(Constants.HEAD).getName();
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to read current version of the configuration", e);
        }
    }

    public static RevCommit getCommit(String commitHash, Repository repository) {
        try {
            return repository.parseCommit(repository.resolve(commitHash));
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to read configuration version '" + commitHash + "'", e);
        }
    }

    public static Path getModelFilepath(Model model, Repository repository) {
        if (model.getId() == null) {
            throw new IllegalArgumentException("ID must be assigned to model before accessing its path");
        }

        return getFilePath(DIRECTORY_MODELS, model.getId(), repository);
    }

    public static Path getNodePath(Node node, Repository repository) {
        if (node.getId() == null) {
            throw new IllegalArgumentException("ID must be assigned to node before accessing its path");
        }
        return getFilePath(DIRECTORY_NODES, node.getId(), repository);
    }

    public static Path getRelationPath(Relation relation, Repository repository) {
        if (relation.getId() == null) {
            throw new IllegalArgumentException("ID must be assigned to relation before accessing its path");
        }
        return getFilePath(DIRECTORY_RELATIONS, relation.getId(), repository);
    }

    public static String generateUniqueID(Repository repository) {
        return Stream.generate(UUID::randomUUID)
                .map(UUID::toString)
                .filter(uuid -> ! fileWithIDExistsInWorkTree(uuid, repository))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Failed to generate unique ID"));
    }

    private static boolean fileWithIDExistsInWorkTree(String id, Repository repository) {
        return Files.exists(getFilePath(DIRECTORY_NODES, id, repository)) ||
                Files.exists(getFilePath(DIRECTORY_RELATIONS, id, repository)) ||
                Files.exists(getFilePath(DIRECTORY_MODELS, id, repository));
    }

    private static Path getFilePath(String directory, String id, Repository repository) {
        return Path.of(repository.getWorkTree().getPath(), directory, id + ".xml");
    }

    public static RepositoryContents<String> getRepositoryContents(Repository repository, RevCommit commit) {
        var repositoryContents = new RepositoryContents<String>();
        try (var treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
//            treeWalk.setFilter(PathFilterGroup.createFromStrings(
//                    FILE_PREFIX_NODE, FILE_PREFIX_RELATION, FILE_PREFIX_MODEL
//            ));
            while (treeWalk.next()) {
                if (treeWalk.isPathPrefix(DIRECTORY_NODES_RAW, DIRECTORY_NODES_RAW.length) == 0) {
                    repositoryContents.nodes().add(readTreeWalkContent(treeWalk));
                } else if (treeWalk.isPathPrefix(DIRECTORY_RELATIONS_RAW, DIRECTORY_RELATIONS_RAW.length) == 0) {
                    repositoryContents.relations().add(readTreeWalkContent(treeWalk));
                } else if (treeWalk.isPathPrefix(DIRECTORY_MODELS_RAW, DIRECTORY_MODELS_RAW.length) == 0) {
                    repositoryContents.models().add(readTreeWalkContent(treeWalk));
                }
            }
            return repositoryContents;
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to read configuration version '" + commit.getName() + "'", e);
        }
    }

    private static String readTreeWalkContent(TreeWalk treewalk) {
        try {
            // 0 is the index of the tree which we obtain the object identifier from
            var byteArray = treewalk.getObjectReader().open(treewalk.getObjectId(0)).getCachedBytes();
            return new String(byteArray, FILE_ENCODING);
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to read content of file '" + treewalk.getPathString() + "'", e);
        }
    }
}
