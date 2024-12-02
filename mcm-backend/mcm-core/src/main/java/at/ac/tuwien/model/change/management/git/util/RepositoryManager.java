package at.ac.tuwien.model.change.management.git.util;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.transformer.DSLTransformer;
import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationReadException;
import at.ac.tuwien.model.change.management.git.exception.ConfigurationWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@Slf4j
@RequiredArgsConstructor
public class RepositoryManager {

    private final GitProperties gitProperties;
    private final DSLTransformer dslTransformer;
    private static final String GIT_DIRECTORY = ".git";

    @FunctionalInterface
    public interface GitCallback<T> {
        T withGit(Git repository);
    }

    @FunctionalInterface
    public interface GitVoidCallback {
        void withGit(Git repository);
    }

    @FunctionalInterface
    public interface RepositoryCallback<T> {
        T withRepository(Repository repository);
    }

    @FunctionalInterface
    public interface RepositoryVoidCallback {
        void withRepository(Repository repository);
    }

    public <T> T withRepository(String repositoryName, boolean mustExist, RepositoryCallback<T> callback) {
        var builder = new FileRepositoryBuilder();
        builder.setGitDir(gitProperties.getRepositoryPath()
                .resolve(RepositoryUtils.sanitizeDirectoryName(repositoryName))
                .resolve(GIT_DIRECTORY).toFile());
        builder.setMustExist(mustExist);
        try (Repository repository = builder.build()) {
            return callback.withRepository(repository);
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to access repository for configuration '" + repositoryName + "'", e);
        }
    }

    public void withRepository(String repositoryName, boolean mustExist, RepositoryVoidCallback callback) {
        withRepository(repositoryName, mustExist, repository -> {
            callback.withRepository(repository);
            return null;
        });
    }

    public <T> T withGit(String repositoryName, GitCallback<T> callback) {
        try (Git git = Git.open(gitProperties.getRepositoryPath()
                .resolve(RepositoryUtils.sanitizeDirectoryName(repositoryName))
                .toFile())) {
            return callback.withGit(git);
        } catch (IOException e) {
            throw new ConfigurationReadException("Failed to open repository for configuration '" + repositoryName + "'", e);
        }
    }

    public void withGit(String repositoryName, GitVoidCallback callback) {
        withGit(repositoryName, git -> {
            callback.withGit(git);
            return null;
        });
    }

    public Path gitRepositoriesPath() {
        return gitProperties.getRepositoryPath();
    }

    public RepositoryContents<Path> writeConfigurationToRepository(Configuration configuration, Repository repository) {
        var repositoryContents = new RepositoryContents<Path>();
        for (var model : Optional.ofNullable(configuration.getModels()).orElse(Set.of())) {
            repositoryContents.models().add(writeModelToFile(model, repository));
            for (var node : Optional.ofNullable(model.getNodes()).orElse(Set.of())) {
                node.setMcmModel(Optional.ofNullable(node.getMcmModel()).orElse(model.getId()));
                repositoryContents.nodes().add(writeNodeToFile(node, repository));
                for (var relation : Optional.ofNullable(node.getRelations()).orElse(Set.of())) {
                    repositoryContents.relations().add(writeRelationToFile(relation, node, repository));
                }
            }
        }
        return repositoryContents;
    }

    public RepositoryContents<Path> updateRepositoryWorkTree(Configuration configuration, Repository repository) {
        RepositoryUtils.clearRepositoryWorkTree(repository);
        return writeConfigurationToRepository(configuration, repository);
    }

    public Configuration readConfigurationFromRepository(String configurationName) {
        return readConfigurationFromRepository(configurationName, Constants.HEAD);
    }

    public Configuration readConfigurationFromRepository(Repository repository) {
        return readConfigurationFromRepository(repository, Constants.HEAD);
    }

    public Configuration readConfigurationFromRepository(String configurationName, String commitHash) {
        return withGit(configurationName, git -> {
            return readConfigurationFromRepository(git.getRepository(), commitHash);
        });
    }

    public AddCommand addRepositoryContents(Repository repository, RepositoryContents<Path> repositoryContents) throws GitAPIException {
            var gitAdd = new AddCommand(repository);
            var repositoryWorkDir = repository.getWorkTree().toPath();
            Stream.of(repositoryContents.models(), repositoryContents.nodes(), repositoryContents.relations())
                    .flatMap(Set::stream)
                    .forEach(path -> gitAdd.addFilepattern(path.relativize(repositoryWorkDir).toString()));
            gitAdd.call();
            return gitAdd;
    }

    public Configuration readConfigurationFromRepository(Repository repository, String commitHash) {
        var configurationName = repository.getWorkTree().getName();

        try {
            var commit = RepositoryUtils.getCommit(commitHash, repository);
            var repositoryContents = RepositoryUtils.getRepositoryContents(repository, commit);
            var nodes = dslTransformer.parseToNodes(repositoryContents.nodes(), repositoryContents.relations());
            var models = repositoryContents.models().stream().map(this::parseDSLToModel).collect(Collectors.toSet());

            for (var node : nodes) {
                var correspondingModels = models.stream().filter(model -> node.getMcmModel().equals(model.getId())).toList();
                if (correspondingModels.size() != 1) {
                    throw new ConfigurationReadException(
                            "Node '" + node.getId() + "' should belong to exactly one model, but belongs to " + correspondingModels.size()
                    );
                }
                var correspondingModel = correspondingModels.getFirst();
                if (correspondingModel.getNodes() == null) correspondingModel.setNodes(new HashSet<>());
                correspondingModel.getNodes().add(node);
            }

            var configuration = new Configuration();
            configuration.setVersion(commit.getName());
            configuration.setName(configurationName);

            // only set models if there are any. Otherwise, leave it null
            // TODO: we may want to instantiate empty lists by default at some point
            configuration.setModels(Optional.of(models)
                    .filter(s -> !s.isEmpty()).orElse(null));

            return configuration;
        } catch (DSLException e) {
            throw new ConfigurationReadException("Failed to read configuration '" + configurationName + "'", e);
        }
    }

    private Path writeModelToFile(Model model, Repository repository) {
        try {
            if (model.getId() == null) {
                model.setId(RepositoryUtils.generateUniqueID(repository));
            }

            var modelPath = RepositoryUtils.getModelFilepath(model, repository);
            return RepositoryUtils.writeRepositoryFile(modelPath, dslTransformer.parseToModelDSL(model));
        } catch (DSLException e) {
            throw new ConfigurationWriteException("Failed to write model '" + model.getId() + "' to repository", e);
        }
    }

    private Path writeNodeToFile(Node node, Repository repository) {
        try {
            if (node.getId() == null) {
                node.setId(RepositoryUtils.generateUniqueID(repository));
            }

            var nodePath = RepositoryUtils.getNodePath(node, repository);
            return RepositoryUtils.writeRepositoryFile(nodePath, dslTransformer.parseToNodeDSL(node));
        } catch (DSLException e) {
            throw new ConfigurationWriteException("Failed to write node '" + node.getId() + "' to repository", e);
        }
    }

    private Path writeRelationToFile(Relation relation, Node sourceNode, Repository repository) {
        try {
            if (relation.getId() == null) {
                relation.setId(RepositoryUtils.generateUniqueID(repository));
            }

            var relationPath = RepositoryUtils.getRelationPath(relation, repository);
            return RepositoryUtils.writeRepositoryFile(relationPath, dslTransformer.parseToRelationDSL(relation, sourceNode));
        } catch (DSLException e) {
            throw new ConfigurationWriteException("Failed to write relation '" + relation.getId() + "' to repository", e);
        }
    }

    private Model parseDSLToModel(String modelDSL) {
        try {
            return dslTransformer.parseToModel(modelDSL);
        } catch (DSLException e) {
            throw new ConfigurationReadException("Failed to parse model DSL", e);
        }
    }
}
