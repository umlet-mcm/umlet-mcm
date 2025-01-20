package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.core.model.versioning.ModelDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.NodeDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.RelationDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationProcessor;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import at.ac.tuwien.model.change.management.git.infrastructure.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

@GitComponent
@RequiredArgsConstructor
@Slf4j
public class ConfigurationRepositoryActionsImpl implements ConfigurationRepositoryActions {

    private static final String NODES_DIRECTORY = "nodes";
    private static final String RELATIONS_DIRECTORY = "relations";
    private static final String MODELS_DIRECTORY = "models";
    private static final String FILE_EXTENSION = ".xml";

    private static final String EXISTING_METADATA_REGEX = "(?s)<metadata>.*?</metadata>";
    private static final String EMPTY_METADATA_REGEX = "<metadata/>";

    private final ConfigurationDSLTransformer configurationDSLTransformer;
    private final VersionNameGenerator versionNameGenerator;

    @Override
    public List<Path> writeConfigurationToWorkingDirectory(
            @NonNull ManagedRepository repository,
            @NonNull Configuration configuration
    ) {
        log.debug("Writing configuration '{}' to repository.", configuration.getName());
        var configurationDSL = configurationDSLTransformer.serializeToDsl(configuration);
        var repositoryFiles = generateRepositoryFiles(configurationDSL);
        var paths = repository.writeRepositoryFiles(repositoryFiles);
        log.debug("Successfully wrote {} files to repository: {}", paths.size(), repository.getName());
        return paths;
    }


    @Override
    public Optional<Configuration> readCurrentConfigurationVersion(@NonNull ManagedRepository repository) {
        log.debug("Reading current configuration from repository: {}", repository.getName());
        return repository.getCurrentRepositoryVersion().map(version -> {
            var configuration = parseRepositoryVersionToConfiguration(repository.getName(), repository.getEncoding(), version);
            var generatedName = findName(version.tags(), true);
            var customName = findName(version.tags(), false);

            configuration.setVersion(new ConfigurationVersion(version.id(), generatedName, customName));
            log.debug("Read current configuration {} from repository.", configuration.getName());
            return configuration;
        }).or(() -> {
            log.debug("No current version found for repository: {}", repository.getName());
            return Optional.empty();
        });
    }

    @Override
    public Optional<Configuration> readConfigurationVersion(@NonNull ManagedRepository repository, @NonNull String version) {
        log.debug("Reading configuration version '{}' from repository: {}", version, repository.getName());
        return repository.getRepositoryVersion(version).map(repositoryVersion -> {
            var configuration = parseRepositoryVersionToConfiguration(repository.getName(), repository.getEncoding(), repositoryVersion);
            log.debug("Read configuration version '{}' from repository: {}", repositoryVersion, repository.getName());
            return configuration;
        }).or(() -> {
            log.debug("No version '{}' found for repository: {}", version, repository.getName());
            return Optional.empty();
        });
    }

    @Override
    public void clearConfigurationRepository(@NonNull ManagedRepository repository) {
        log.debug("Clearing repository: {}", repository.getName());
        repository.deleteRepositoryFiles(
                Path.of(MODELS_DIRECTORY),
                Path.of(NODES_DIRECTORY),
                Path.of(RELATIONS_DIRECTORY)
        );
        log.debug("Cleared repository: {}", repository.getName());
    }

    @Override
    public ConfigurationContents<ModelDiff, NodeDiff, RelationDiff> compareConfigurationVersions(
            @NonNull ManagedRepository repository,
            @NonNull String oldVersion,
            @NonNull String newVersion,
            boolean includeUnchanged
    ) {
        log.debug("Comparing configuration versions '{}' and '{}' in repository: {}", oldVersion, newVersion, repository.getName());
        var diffEntries = repository.versioning().compareVersions(oldVersion, newVersion, includeUnchanged, managedRepositoryObject -> {
            var content = managedRepositoryObject.getFileContent();
            var modifiedContent = content
                    .replaceAll(EXISTING_METADATA_REGEX, "")
                    .replaceAll(EMPTY_METADATA_REGEX, "");
            return modifiedContent.getBytes(repository.getEncoding());
        });

        // generally far from ideal to read both versions here, but likely the only way to avoid errors
        // given current implementation of DSLTransformer
        // alternative would be either a dangerous regex based parsing of the DSL
        // or changes to DSLTransformer
        var oldConfiguration = readConfigurationVersion(repository, oldVersion)
                .orElseThrow(() -> new RepositoryReadException("Old configuration version not found in repository: " + oldVersion));
        var newConfiguration = readConfigurationVersion(repository, newVersion)
                .orElseThrow(() -> new RepositoryReadException("New configuration version not found in repository: " + newVersion));

        var configurationContents = getDiffDomainModels(diffEntries, oldConfiguration, newConfiguration);

        log.debug("Compared configuration versions '{}' and '{}' in repository: {}", oldVersion, newVersion, repository.getName());
        return configurationContents;
    }

    @Override
    public String commitConfigurationChanges(@NonNull ManagedRepository managedRepository, @NonNull String commitMessage, String customTag) {
        log.debug("Committing configuration changes to repository: {}", managedRepository.getName());
        var changes = managedRepository.versioning().stageAll();
        var commitHash = managedRepository.versioning().commit(defaultCommitMessage(
                        managedRepository.getName(), changes),
                true
        );
        var existingTags = managedRepository.versioning().listTags();
        var newAutoVersionTag = versionNameGenerator.findNextVersionName(existingTags);
        managedRepository.versioning().tagCommit(commitHash, newAutoVersionTag);

        if (customTag != null && !customTag.equals(newAutoVersionTag)) {
            managedRepository.versioning().tagCommit(commitHash, customTag);
        }
        log.info("Committed {} configuration changes to repository: {}", changes, managedRepository.getName());

        return commitHash;
    }

    @Override
    public void renameConfigurationRepository(@NonNull ManagedRepository repository, @NonNull String newName) {
        log.debug("Renaming repository '{}' to '{}'", repository.getName(), newName);
        repository.renameRepository(newName);
        log.info("Renamed repository '{}' to '{}'", repository.getName(), newName);
    }

    private Set<ManagedRepositoryFile> generateRepositoryFiles(
            ConfigurationContents<DSLElement<Model>, DSLElement<Node>, DSLElement<Relation>> configurationContents
    ) {
        var repositoryFiles = new HashSet<ManagedRepositoryFile>();
        repositoryFiles.addAll(configurationContents.getModels().stream().map(
                model -> generateRepositoryFile(MODELS_DIRECTORY, model)).toList()
        );
        repositoryFiles.addAll(
                configurationContents.getNodes().stream().map(node -> generateRepositoryFile(NODES_DIRECTORY, node)).toList()
        );
        repositoryFiles.addAll(
                configurationContents.getRelations().stream().map(relation -> generateRepositoryFile(RELATIONS_DIRECTORY, relation)).toList()
        );
        return repositoryFiles;
    }

    private ManagedRepositoryFile generateRepositoryFile(String dir, DSLElement<?> dslElement) {
        return new ManagedRepositoryFile(getElementPath(dir, dslElement.element()), dslElement.dsl());
    }

    private Path getElementPath(String dir, BaseAttributes el) {
        return Path.of(dir, el.getId() + FILE_EXTENSION);
    }

    private Configuration parseRepositoryVersionToConfiguration(
            String name,
            Charset encoding,
            ManagedRepositoryVersion version
    ) {
        var configurationDSL = getRepositoryContents(version.objects(), encoding);
        var versionName = findName(version.tags(), true);
        var versionCustomName = findName(version.tags(), false);
        var configurationVersion = new ConfigurationVersion(version.id(), versionName, versionCustomName);

        return configurationDSLTransformer.parseToConfiguration(configurationDSL, name, configurationVersion);
    }

    private ConfigurationContents<String, String, String> getRepositoryContents(Collection<ManagedRepositoryObject> objects, Charset encoding) {

        var rawModelsDirectory = MODELS_DIRECTORY.getBytes(encoding);
        var rawNodesDirectory = NODES_DIRECTORY.getBytes(encoding);
        var rawRelationsDirectory = RELATIONS_DIRECTORY.getBytes(encoding);

        var configurationContents = new ConfigurationContents<String, String, String>();
        for (var repositoryObj : objects) {
            if (repositoryObj.rawFilePathMatches(rawModelsDirectory)) {
                configurationContents.addModel(repositoryObj.getFileContent());
            } else if (repositoryObj.rawFilePathMatches(rawNodesDirectory)) {
                configurationContents.addNode(repositoryObj.getFileContent());
            } else if (repositoryObj.rawFilePathMatches(rawRelationsDirectory)) {
                configurationContents.addRelation(repositoryObj.getFileContent());
            }
        }
        return configurationContents;
    }

    private ConfigurationContents<ModelDiff, NodeDiff, RelationDiff> getDiffDomainModels(
            Collection<ManagedDiffEntry> diffEntries,
            Configuration oldConfiguration,
            Configuration newConfiguration
    ) {
        var oldConfigurationProcessor = new ConfigurationProcessor(oldConfiguration);
        var newConfigurationProcessor = new ConfigurationProcessor(newConfiguration);
        var configurationContents = new ConfigurationContents<ModelDiff, NodeDiff, RelationDiff>();

        for (var diff : diffEntries) {
            var lookupProcessor = selectAffectedVersionForDiffObject(diff.getAffectedObjectType(), oldConfigurationProcessor, newConfigurationProcessor);
            var filePath = diff.getAffectedObject().getFilePath();
            var id = extractIDFromPath(filePath);

            if (filePath.contains(MODELS_DIRECTORY)) {
                var model = lookupProcessor.getModelByID(id)
                        .orElseThrow(() -> new RepositoryReadException("Model with ID '" + id + "' referenced by diff entry not found in configuration"));
                configurationContents.addModel(new ModelDiff(model, diff.getDiffType().toString(), diff.getDiff()));
            } else if (filePath.contains(NODES_DIRECTORY)) {
                var node = lookupProcessor.getNodeByID(id)
                        .orElseThrow(() -> new RepositoryReadException("Node with ID '" + id + "' referenced by diff entry not found in configuration"));
                configurationContents.addNode(new NodeDiff(node, diff.getDiffType().toString(), diff.getDiff()));
            } else if (filePath.contains(RELATIONS_DIRECTORY)) {
                var relation = lookupProcessor.getRelationByID(id)
                        .orElseThrow(() -> new RepositoryReadException("Relation with ID '" + id + "' referenced by diff entry not found in configuration"));
                configurationContents.addRelation(new RelationDiff(relation, diff.getDiffType().toString(), diff.getDiff()));
            } else {
                throw new RepositoryReadException("Could not determine affected object type from file path: " + filePath);
            }
        }

        return configurationContents;
    }

    private <T> T selectAffectedVersionForDiffObject(AffectedObjectType type, T oldOb, T newOb) {
        return switch (type) {
            case NEW, BOTH -> newOb;
            case OLD -> oldOb;
        };
    }

    private String extractIDFromPath(String path) {
        return Path.of(path).getFileName().toString().replace(FILE_EXTENSION, "");
    }

    private String defaultCommitMessage(String configurationName, int numberOfChanges) {
        return "Updated configuration '" + configurationName + "' with " + numberOfChanges + " changes staged";
    }

    private String findName(List<String> tags, boolean isGenerated) {
        return tags.stream()
                .filter(tag -> isGenerated == versionNameGenerator.isAutoGeneratedVersionName(tag))
                .findFirst()
                .orElse(null);
    }
}
