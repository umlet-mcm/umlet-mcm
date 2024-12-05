package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.transformer.DSLTransformer;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryWriteException;
import at.ac.tuwien.model.change.management.git.util.RepositoryContents;
import at.ac.tuwien.model.change.management.git.util.RepositoryUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@GitComponent
@RequiredArgsConstructor
@Slf4j
public class ConfigurationIOManagerImpl implements ConfigurationIOManager {

    private final DSLTransformer dslTransformer;

    private static final Charset FILE_ENCODING = StandardCharsets.UTF_8;
    private static final String FILE_EXTENSION = ".xml";
    private static final String NODES_DIRECTORY = "nodes";
    private static final String RELATIONS_DIRECTORY = "relations";
    private static final String MODELS_DIRECTORY = "models";


    @Override
    public void clearConfigurationRepository(@NonNull Repository repository) {
        try {
            log.debug("Clearing repository '{}'.", RepositoryUtils.getRepositoryName(repository));
            var repositoryPath = repository.getWorkTree().toPath();
            FileSystemUtils.deleteRecursively(repositoryPath.resolve(NODES_DIRECTORY));
            FileSystemUtils.deleteRecursively(repositoryPath.resolve(RELATIONS_DIRECTORY));
            FileSystemUtils.deleteRecursively(repositoryPath.resolve(MODELS_DIRECTORY));
        } catch (IOException e) {
            throw new RepositoryWriteException("Could not clear repository '" + RepositoryUtils.getRepositoryName(repository) + "'.", e);
        }
    }

    @Override
    public RepositoryContents<Path> writeConfigurationToRepository(
            @NonNull Repository repository,
            @NonNull Configuration configuration
    ) {
        try {
            log.debug("Writing configuration to repository '{}'.", RepositoryUtils.getRepositoryName(repository));
            assignMissingIDs(repository, configuration);
            var repositoryContents = writeToRepository(repository, configuration);
            log.info("Successfully wrote configuration to repository '{}', including {} models, {} nodes and {} relations.",
                    RepositoryUtils.getRepositoryName(repository),
                    repositoryContents.models().size(),
                    repositoryContents.nodes().size(),
                    repositoryContents.relations().size());
            return repositoryContents;
        } catch (IOException e) {
            throw new RepositoryWriteException(
                    "Failed to write configuration '" + configuration.getName() +
                            "' to repository '" + RepositoryUtils.getRepositoryName(repository) + "'", e
            );
        }
    }

    @Override
    public Configuration readConfigurationFromRepository(@NonNull Repository repository, @NonNull String version) {
        var repositoryName = RepositoryUtils.getRepositoryName(repository);

        try {
            log.debug("Reading configuration from repository '{}'.", RepositoryUtils.getRepositoryName(repository));
            var commit = repository.parseCommit(RepositoryUtils.resolveCommit(repository, version)
                    .orElseThrow(() -> new RepositoryReadException("Could not resolve commit '" + version + "' in repository '" + repositoryName + "'")));
            var dslContents = readRepositoryContents(repository, commit);
            var models = new HashSet<Model>();
            for (var modelDSL : dslContents.models()) {
                models.add(dslTransformer.parseToModel(modelDSL));
            }

            var nodes = dslTransformer.parseToNodes(dslContents.nodes(), dslContents.relations());

            for (var node : nodes) {
                var refModel = models.stream()
                        .filter(model -> Objects.equals(model.getId(), node.getMcmModel()))
                        .findFirst()
                        .orElseThrow(() -> new RepositoryReadException(
                                "Could not find a model with ID: '" + node.getMcmModel() + "' referenced by node '" + node.getId() + "'"
                        ));
                refModel.setNodes(Optional.ofNullable(refModel.getNodes()).orElse(new HashSet<>()));
                refModel.getNodes().add(node);
            }

            var configuration = new Configuration();
            // repositoryName should always correspond to configuration name
            configuration.setName(repositoryName);
            configuration.setVersion(commit.getName());
            // just to be consistent with written domain model whose models parameter may also be null
            // TODO: we probably want to initialize these parameters to empty sets by default
            if (!models.isEmpty()) configuration.setModels(models);
            log.info("Successfully read configuration from repository '{}'.", repositoryName);
            return configuration;
        } catch (IOException e) {
            throw new RepositoryReadException("Failed to read repository '" + repositoryName + "'", e);
        }
    }

    private void assignMissingIDs(Repository repository, Configuration configuration) {
        for (var model : tryAccessCollection(configuration.getModels())) {
            if (model.getId() == null) model.setId(generateIdUniqueToRepository(repository));
            for (var node : tryAccessCollection(model.getNodes())) {
                if (node.getId() == null) node.setId(generateIdUniqueToRepository(repository));
                if (node.getMcmModel() == null) node.setMcmModel(model.getId());
                for (var relation : tryAccessCollection(node.getRelations())) {
                    if (relation.getId() == null) relation.setId(generateIdUniqueToRepository(repository));
                }
            }
        }
    }

    private RepositoryContents<Path> writeToRepository(Repository repository, Configuration configuration) throws IOException {
        var repositoryContents = new RepositoryContents<Path>();
        for (var model : tryAccessCollection(configuration.getModels())) {
            repositoryContents.models().add(writeModelToFile(model, repository));
            for (var node : tryAccessCollection(model.getNodes())) {
                repositoryContents.nodes().add(writeNodeToFile(node, repository));
                for (var relation : tryAccessCollection(node.getRelations())) {
                    repositoryContents.relations().add(writeRelationToFile(relation, node, repository));
                }
            }
        }
        return repositoryContents;
    }

    private <T> Collection<T> tryAccessCollection(Collection<T> collection) {
        return Optional.ofNullable(collection).orElse(Collections.emptyList());
    }

    private Path writeNodeToFile(Node node, Repository repository) throws IOException {
        if (node.getId() == null) {
            node.setId(generateIdUniqueToRepository(repository));
        }
        var nodePath = getRepositoryFile(repository, NODES_DIRECTORY, node.getId());
        var nodeContent = dslTransformer.parseToNodeDSL(node);
        return writeTextToFile(nodePath, nodeContent);
    }

    private Path writeRelationToFile(Relation relation, Node sourceNode, Repository repository) throws IOException {
        if (relation.getId() == null) {
            relation.setId(generateIdUniqueToRepository(repository));
        }
        var relationPath = getRepositoryFile(repository, RELATIONS_DIRECTORY, relation.getId());
        var relationContent = dslTransformer.parseToRelationDSL(relation, sourceNode);
        return writeTextToFile(relationPath, relationContent);
    }

    private Path writeModelToFile(Model model, Repository repository) throws IOException {
        if (model.getId() == null) {
            model.setId(generateIdUniqueToRepository(repository));
        }
        var modelPath = getRepositoryFile(repository, MODELS_DIRECTORY, model.getId());
        var modelContent = dslTransformer.parseToModelDSL(model);
        return writeTextToFile(modelPath, modelContent);
    }

    private String generateIdUniqueToRepository(Repository repository) {
        return Stream.generate(UUID::randomUUID)
                .map(UUID::toString)
                .filter(id -> !idExistsInRepository(id, repository))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not generate unique id in repository '" +
                        RepositoryUtils.getRepositoryName(repository) + "'"));
    }

    private boolean idExistsInRepository(String id, Repository repository) {
        return Files.exists(getRepositoryFile(repository, NODES_DIRECTORY, id)) ||
                Files.exists(getRepositoryFile(repository, RELATIONS_DIRECTORY, id)) ||
                Files.exists(getRepositoryFile(repository, MODELS_DIRECTORY, id));
    }

    private Path getRepositoryFile(Repository repository, String directory, String id) {
        return repository.getWorkTree().toPath().resolve(directory).resolve(id + FILE_EXTENSION);
    }

    private Path writeTextToFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.writeString(path, content, FILE_ENCODING);
    }

    private RepositoryContents<String> readRepositoryContents(Repository repository, RevCommit commit) throws IOException {
        var repositoryContents = new RepositoryContents<String>();
        var models_directory_raw = MODELS_DIRECTORY.getBytes(FILE_ENCODING);
        var nodes_directory_raw = NODES_DIRECTORY.getBytes(FILE_ENCODING);
        var relations_directory_raw = RELATIONS_DIRECTORY.getBytes(FILE_ENCODING);

        RepositoryUtils.walkCommit(repository, commit, treeWalk -> {
            if (RepositoryUtils.treeWalkIsInDirectory(treeWalk, models_directory_raw)) {
                repositoryContents.models().add(readTreeWalk(treeWalk));
            } else if (RepositoryUtils.treeWalkIsInDirectory(treeWalk, nodes_directory_raw)) {
                repositoryContents.nodes().add(readTreeWalk(treeWalk));
            } else if (RepositoryUtils.treeWalkIsInDirectory(treeWalk, relations_directory_raw)) {
                repositoryContents.relations().add(readTreeWalk(treeWalk));
            }
        });
        return repositoryContents;
    }

    private String readTreeWalk(TreeWalk treeWalk) throws IOException {
        var content = RepositoryUtils.getTreeWalkContent(treeWalk).orElseThrow(
                () -> new IOException("Failed to read content of file '" + treeWalk.getPathString() + "'")
        );
        return new String(content, FILE_ENCODING);
    }
}
