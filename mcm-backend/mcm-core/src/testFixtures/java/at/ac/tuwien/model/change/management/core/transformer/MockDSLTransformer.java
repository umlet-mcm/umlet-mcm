package at.ac.tuwien.model.change.management.core.transformer;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MockDSLTransformer implements DSLTransformer {

    private final HashMap<String, BaseAttributes> storedElements = new HashMap<>();

    @Override
    public Set<Node> parseToNodes(Set<String> nodes, Set<String> relations) {
        return nodes.stream().map(storedElements::get).map(Node.class::cast).collect(Collectors.toSet());
    }

    @Override
    public String parseToNodeDSL(Node node) {
        var uuid = generateUniqueKey();
        storedElements.put(uuid, node);
        return uuid;
    }

    @Override
    public String parseToRelationDSL(Relation relation, Node source) {
        var uuid = generateUniqueKey();
        storedElements.put(uuid, relation);
        return uuid;
    }

    @Override
    public Model parseToModel(String metadata) {
        return (Model) storedElements.get(metadata);
    }

    @Override
    public String parseToModelDSL(Model model) {
        var uuid = generateUniqueKey();
        storedElements.put(uuid, model);
        return uuid;
    }

    private String generateUniqueKey() {
        return Stream.generate(UUID::randomUUID)
                .map(UUID::toString)
                .filter(uuid -> !storedElements.containsKey(uuid))
                .findFirst()
                .orElseThrow();
    }
}

