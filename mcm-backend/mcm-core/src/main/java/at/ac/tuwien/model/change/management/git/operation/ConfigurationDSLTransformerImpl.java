package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.transformer.DSLTransformer;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationProcessor;
import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryWriteException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@GitComponent
@RequiredArgsConstructor
public class ConfigurationDSLTransformerImpl implements ConfigurationDSLTransformer {

    private final DSLTransformer dslTransformer;
    private final IdGenerator idGenerator;

    @Override
    public Configuration parseToConfiguration(
            @NonNull Set<String> models,
            @NonNull Set<String> nodes,
            @NonNull Set<String> relations,
            @Nullable String configurationName,
            @Nullable ConfigurationVersion configurationVersion) {
        log.debug("Parsing configuration with name '{}' and version '{}', " +
                        "consisting of {} models, {} nodes and {} relations.",
                configurationName, configurationVersion, models.size(), nodes.size(), relations.size());
        var parsedConfiguration = new Configuration();
        parsedConfiguration.setName(configurationName);
        parsedConfiguration.setVersion(configurationVersion);
        var contents = parseToConfigurationContents(models, nodes, relations);
        var modelsWithNodes = assignNodesToModels(contents.getModels(), contents.getNodes());
        parsedConfiguration.setModels(modelsWithNodes);
        log.debug("Successfully parsed configuration with name '{}' and version '{}', " +
                        "consisting of {} models, {} nodes and {} relations.",
                configurationName, configurationVersion, contents.getModels().size(), contents.getNodes().size(),
                contents.getRelations().size());
        return parsedConfiguration;
    }

    @Override
    public ConfigurationContents<DSLElement<Model>, DSLElement<Node>, DSLElement<Relation>> serializeToDsl(
            @NonNull Configuration configuration
    ) {
        log.debug("Serializing configuration with name '{}' to DSL", configuration.getName());

        var configurationContents = new ConfigurationContents<DSLElement<Model>, DSLElement<Node>, DSLElement<Relation>>();
        var configurationProcessor = new ConfigurationProcessor(configuration);

        configurationProcessor.processModels(model -> configurationContents.addModel(toDSLEl(model)));
        configurationProcessor.processNodes((node, model) -> configurationContents.addNode(toDSLEl(node, model)));
        configurationProcessor.processRelations(
                (relation, source) -> configurationContents.addRelation(toDSLEl(relation, source))
        );

        log.debug("Successfully serialized configuration with name '{}' to DSL containing {} models, {} nodes and {} relations",
                configuration.getName(), configurationContents.getModels().size(), configurationContents.getNodes().size(),
                configurationContents.getRelations().size());
        return configurationContents;
    }

    private ConfigurationContents<Model, Node, Relation> parseToConfigurationContents(
            Set<String> models,
            Set<String> nodes,
            Set<String> relations) {
        var configurationContents = new ConfigurationContents<Model, Node, Relation>();
        configurationContents.setModels(models.stream().map(this::parseToModel).collect(Collectors.toSet()));
        configurationContents.setNodes(parseToNodes(nodes, relations));
        return configurationContents;
    }

    private Model parseToModel(String modelDSL) {
        try {
            return dslTransformer.parseToModel(modelDSL);
        } catch (DSLException e) {
            throw new RepositoryReadException("Failed to parse model DSL to Model domain object", e);
        }
    }

    private Set<Node> parseToNodes(Set<String> nodeDSLs, Set<String> relationDSLs) {
        try {
            return dslTransformer.parseToNodes(nodeDSLs, relationDSLs);
        } catch (DSLException e) {
            throw new RepositoryReadException("Failed to parse node and relation DSLs to Node and Relation domain objects", e);

        }
    }

    private Set<Model> assignNodesToModels(Set<Model> models, Set<Node> nodes) {
        HashMap<String, Model> modelLookup = new HashMap<>();

        for (Model model : models) {
            if (modelLookup.containsKey(model.getId())) {
                throw new RepositoryReadException("Duplicate model ID found: " + model.getId());
            }
            modelLookup.put(model.getId(), model);
        }

        for (var node : nodes) {
            var model = modelLookup.get(node.getMcmModelId());
            if (model == null) {
                throw new RepositoryReadException("Could not assign node with ID '" + node.getId() +
                        "' to model with ID '" + node.getMcmModelId() + "' because no such model was found");
            }
            if (model.getNodes() == null) {
                model.setNodes(new HashSet<>());
            }
            model.getNodes().add(node);
        }
        return models;
    }

    private DSLElement<Model> toDSLEl(Model model) {
        return new DSLElement<>(model, serializeModel(model));
    }

    private DSLElement<Node> toDSLEl(Node node, Model model) {
        return new DSLElement<>(node, serializeNode(node, model));
    }

    private DSLElement<Relation> toDSLEl(Relation relation, Node source) {
        return new DSLElement<>(relation, serializeRelation(relation, source));
    }

    private String serializeModel(Model model) {
        try {
            if (model.getId() == null) idGenerator.setID(model);
            return dslTransformer.parseToModelDSL(model);
        } catch (DSLException e) {
            throw new RepositoryWriteException("Failed to serialize model to DSL: " + model.getId(), e);
        }
    }

    private String serializeNode(Node node, Model model) {
        try {
            if (node.getId() == null) idGenerator.setID(node);
            if (node.getMcmModelId() == null) node.setMcmModelId(model.getId());
            return dslTransformer.parseToNodeDSL(node);
        } catch (DSLException e) {
            throw new RepositoryWriteException("Failed to serialize node to DSL: " + node.getId(), e);
        }
    }

    private String serializeRelation(Relation relation, Node source) {
        try {
            if (relation.getId() == null) idGenerator.setID(relation);
            if (relation.getMcmModelId() == null) relation.setMcmModelId(source.getMcmModelId());
            return dslTransformer.parseToRelationDSL(relation, source);
        } catch (DSLException e) {
            throw new RepositoryWriteException("Failed to serialize relation to DSL: " + relation.getId(), e);
        }
    }
}