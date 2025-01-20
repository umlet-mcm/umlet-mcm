package at.ac.tuwien.model.change.management.core.utils;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static at.ac.tuwien.model.change.management.core.utils.CollectionUtils.tryAccessSet;

@RequiredArgsConstructor
public class ConfigurationProcessor {
    @NonNull
    private final Configuration configuration;

    private final IDCache idCache = new IDCache();

    public void processModels(@NonNull Consumer<Model> modelConsumer) {
        tryAccessSet(configuration.getModels()).forEach(modelConsumer);
    }

    public void processNodes(@NonNull BiConsumer<Node, Model> nodeConsumer) {
        processModels(model -> tryAccessSet(model.getNodes()).forEach(node -> nodeConsumer.accept(node, model)));
    }

    public void processRelations(@NonNull BiConsumer<Relation, Node> relationConsumer) {
        processNodes((node, model)  -> tryAccessSet(node.getRelations()).forEach(relation -> relationConsumer.accept(relation, node)));
    }

    public Optional<BaseAttributes> getByID(@NonNull String id) {
        return getModelByID(id).map(t -> (BaseAttributes) t)
                .or(() -> getNodeByID(id))
                .or(() -> getRelationByID(id));
    }

    public Optional<Model> getModelByID(@NonNull String id) {
        return Optional.ofNullable(idCache.getModelIDs().get(id));
    }

    public Optional<Node> getNodeByID(@NonNull String id) {
        return Optional.ofNullable(idCache.getNodeIDs().get(id));
    }

    public Optional<Relation> getRelationByID(@NonNull String id) {
        return Optional.ofNullable(idCache.getRelationIDs().get(id));
    }

    private class IDCache {
        @Getter(lazy = true)
        private final Map<String, Model> modelIDs = computeModelIDs();

        @Getter(lazy = true)
        private final Map<String, Node> nodeIDs = computeNodeIDs();

        @Getter(lazy = true)
        private final Map<String, Relation> relationIDs = computeRelationIDs();

        private Map<String, Model> computeModelIDs() {
            var ids = new HashMap<String, Model>();
            processModels(model -> ids.put(model.getId(), model));
            return ids;
        }

        private Map<String, Node> computeNodeIDs() {
            var ids = new HashMap<String, Node>();
            processNodes((node, model) -> ids.put(node.getId(), node));
            return ids;
        }

        private Map<String, Relation> computeRelationIDs() {
            var ids = new HashMap<String, Relation>();
            processRelations((relation, node) -> ids.put(relation.getId(), relation));
            return ids;
        }
    }
}
