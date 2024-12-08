package at.ac.tuwien.model.change.management.server.util;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MappingUtils {

    private MappingUtils() {
    }

    public static Map<String, Node> createNodesLookupTable(@NonNull Configuration configuration) {
        return nodesStream(configuration)
                .filter(node -> node.getId() != null)
                .collect(Collectors.toMap(Node::getId, node -> node));
    }

    public static Map<String, Relation> createRelationsLookupTable(@NonNull Configuration configuration) {
        return nodesStream(configuration)
                .flatMap(node -> Optional.ofNullable(node.getRelations()).stream().flatMap(Collection::stream))
                .filter(Objects::nonNull)
                .filter(relation -> relation.getId() != null)
                .collect(Collectors.toMap(Relation::getId, relation -> relation));
    }

    private static Stream<Node> nodesStream(Configuration configuration) {
        return Optional.ofNullable(configuration.getModels()).stream().flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .flatMap(model -> Optional.ofNullable(model.getNodes()).stream().flatMap(Collection::stream))
                .filter(Objects::nonNull);
    }
}
