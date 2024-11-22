package at.ac.tuwien.model.change.management.git.util;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationWriteException;
import at.ac.tuwien.model.change.management.git.exception.IllegalNameException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class RepositoryUtils {

    public static boolean repositoryExists(Repository repository) {
        return repository.getObjectDatabase() != null;
    }

    public static String headCommitHash(Repository repository) throws IOException {
        return repository.resolve(Constants.HEAD).getName();
    }

    public static Set<Path> writeConfigurationToRepository(Configuration configuration, Path repositoryWorkDir) {
            return configuration.getModels().stream()
                    .flatMap(model -> writeModelToRepository(model, repositoryWorkDir).stream())
                    .collect(Collectors.toSet());
    }

    private static Set<Path> writeModelToRepository(Model model, Path repositoryWorkDir) {
        try {
            var modelDirectoryName = FSEncodingUtils.escapeStringAsDirectoryName(model.getId());
            var modelDirectoryPath = Files.createDirectories(repositoryWorkDir.resolve(modelDirectoryName));
            return model.getNodes().stream()
                    .flatMap(node -> writeNodeToRepository(node, modelDirectoryPath).stream())
                    .collect(Collectors.toSet());
        } catch (IllegalNameException e) {
            throw new ConfigurationWriteException("Could not encode name of model '" + model.getId() + "' as a directory path", e);
        } catch (IOException e) {
            throw new ConfigurationWriteException("Failed to write model '" + model.getId() + "' to repository", e);
        }
    }

    private static Set<Path> writeNodeToRepository(Node node, Path modelWorkDir) {
        // TODO: implement. Don't forget to also add relations
        return Set.of();
    }
}
