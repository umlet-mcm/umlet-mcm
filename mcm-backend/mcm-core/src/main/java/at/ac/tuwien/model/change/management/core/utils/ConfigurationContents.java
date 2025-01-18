package at.ac.tuwien.model.change.management.core.utils;

import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
public class ConfigurationContents<M, N, R> {

    private Set<M> models;
    private Set<N> nodes;
    private Set<R> relations;

    public Set<M> getModels() {
        if (models == null) models = new HashSet<>();
        return models;
    }

    public Set<N> getNodes() {
        if (nodes == null) nodes = new HashSet<>();
        return nodes;
    }

    public Set<R> getRelations() {
        if (relations == null) relations = new HashSet<>();
        return relations;
    }

    public void addModel(M model) {
        if (models == null) models = new HashSet<>();
        this.models.add(model);
    }

    public void addNode(N node) {
        if (nodes == null) nodes = new HashSet<>();
        this.nodes.add(node);
    }

    public void addRelation(R relation) {
        if (relations == null) relations = new HashSet<>();
        this.relations.add(relation);
    }
}
