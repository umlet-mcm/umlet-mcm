package at.ac.tuwien.model.change.management.testutil;

import at.ac.tuwien.model.change.management.core.model.*;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DomainModelGen {

    private static final int MAX_TITLE_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 20;
    private static final int MAX_TAG_NUMBER = 5;
    private static final int MAX_TAG_LENGTH = 5;
    private static final int MAX_PPR_TYPE_LENGTH = 10;
    private static final int MAX_MCM_ATTRIBUTE_NUMBER = 5;
    private static final int MAX_MCM_ATTRIBUTE_KEY_LENGTH = 5;
    private static final int MAX_MCM_ATTRIBUTE_VALUE_LENGTH = 5;
    private static final int MAX_GENERATED_ATTRIBUTE_NUMBER = 10;
    private static final int MAX_GENERATED_ATTRIBUTE_VALUE = 100;
    private static final int MAX_RELATIVE_MID_POINTS = 5;
    private static final int MAX_RELATIVE_POSITION = 100;
    private static final int MAX_POINT = 1000;
    private static final int MAX_DIMENSION = 100;

    private enum ElementType {
        NODE,
        RELATION
    }

    public static Configuration generateRandomizedConfiguration(
            @NonNull String name,
            int numberModels,
            int numberNodesPerModel,
            int numberRelationsPerNode) {
        return generateRandomizedConfiguration(
                name,
                numberModels,
                numberModels,
                numberNodesPerModel,
                numberNodesPerModel,
                numberRelationsPerNode,
                numberRelationsPerNode
        );
    }

    public static Configuration generateRandomizedConfiguration(
            @NonNull String name,
            int minNumberModels,
            int maxNumberModels,
            int minNumberNodesPerModel,
            int maxNumberNodesPerModel,
            int minNumberRelationsPerNode,
            int maxNumberRelationsPerNode) {
        if (minNumberModels < 0) {
            throw new IllegalArgumentException("Number of models must be non-negative.");
        }
        if (minNumberModels > maxNumberModels) {
            throw new IllegalArgumentException("Minimum number of models must be less than or equal to maximum number.");
        }
        var configuration = new Configuration();
        configuration.setName(name);
        configuration.setModels(new HashSet<>());
        var upperBoundModels = maxNumberModels - minNumberModels + 1;
        var numModels = getRandomInt(upperBoundModels) + minNumberModels;

        for (int i = 0; i < numModels; i++) {
            configuration.getModels()
                    .add(generateRandomizedModel(
                            minNumberNodesPerModel,
                            maxNumberNodesPerModel,
                            minNumberRelationsPerNode,
                            maxNumberRelationsPerNode
                    ));
        }
        return configuration;
    }

    public static Model generateRandomizedModel(int numberNodes, int numberRelationsPerNode) {
        return generateRandomizedModel(numberNodes, numberRelationsPerNode, numberNodes, numberRelationsPerNode);
    }

    public static Model generateRandomizedModel(
            int minNumberNodes,
            int maxNumberNodes,
            int minNumberRelationsPerNode,
            int maxNumberRelationsPerNode) {

        if (minNumberNodes < 0 || minNumberRelationsPerNode < 0) {
            throw new IllegalArgumentException("Number of nodes and relations must be non-negative.");
        }

        if (minNumberNodes > maxNumberNodes || minNumberRelationsPerNode > maxNumberRelationsPerNode) {
            throw new IllegalArgumentException("Minimum number of nodes and relations must be less than or equal to maximum number.");
        }

        if (maxNumberRelationsPerNode >= minNumberNodes && minNumberNodes > 0) {
            throw new IllegalArgumentException("Maximum number of relations per node must be less than the minimum number of nodes.");
        }

        int upperBoundNodes = maxNumberNodes - minNumberNodes + 1;
        int upperBoundRelations = maxNumberRelationsPerNode - minNumberRelationsPerNode + 1;
        var model = generateRandomizedModel();
        int numNodes = getRandomInt(upperBoundNodes) + minNumberNodes;

        for (int i = 0; i < numNodes; i++) {
            var node = generateRandomizedNode();
            if (model.getNodes() == null) model.setNodes(new HashSet<>());
            model.getNodes().add(node);
        }

        for (var node : Optional.ofNullable(model.getNodes()).orElseGet(Collections::emptySet)) {
            int numRelations = getRandomInt(upperBoundRelations) + minNumberRelationsPerNode;
            for (int i = 0; i < numRelations; i++) {
                var relation = generateRandomizedRelation(
                        model.getNodes().stream().filter(n -> !Objects.equals(node, n)).toList()
                );
                if (node.getRelations() == null) node.setRelations(new HashSet<>());
                node.getRelations().add(relation);
            }
        }

        return model;
    }

    public static Model generateRandomizedModel() {
        var model = new Model();
        model.setTitle(RandomStringUtils.randomAlphabetic(MAX_TITLE_LENGTH));
        model.setDescription(RandomStringUtils.randomAlphabetic(MAX_DESCRIPTION_LENGTH));
        return model;
    }


    public static Node generateRandomizedNode() {
        var node = new Node();
        node.setUmletPosition(generateRandomizedUMLetPosition());
        node.setTitle(RandomStringUtils.randomAlphabetic(MAX_TITLE_LENGTH));
        node.setTags((IntStream.range(0, getRandomInt(MAX_TAG_NUMBER + 1)).mapToObj(
                i -> RandomStringUtils.randomAlphabetic(MAX_TAG_LENGTH))).toList()
        );
        node.setDescription(RandomStringUtils.randomAlphabetic(MAX_DESCRIPTION_LENGTH));
        node.setElementType(ElementType.NODE.toString());
        node.setPprType(RandomStringUtils.randomAlphabetic(MAX_PPR_TYPE_LENGTH));
        node.setMcmAttributes(IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                key -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                value -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                (existing, replacement) -> replacement,
                LinkedHashMap::new
        )));
        node.setGeneratedAttributes(IntStream.range(0, getRandomInt(MAX_GENERATED_ATTRIBUTE_NUMBER + 1))
                .mapToObj(i -> getRandomInt(MAX_GENERATED_ATTRIBUTE_VALUE + 1)).toList());
        return node;
    }

    public static Relation generateRandomizedRelation(List<Node> availableTargets) {
        var relation = new Relation();
        var randomTargetIndex = getRandomInt(availableTargets.size());
        relation.setTarget(availableTargets.get(randomTargetIndex));
        relation.setType(ElementType.RELATION.toString());
        relation.setUmletPosition(generateRandomizedUMLetPosition());
        relation.setRelativeStartPoint(generateRandomizedRelativePosition());
        relation.setRelativeMidPoints(IntStream.range(0, getRandomInt(MAX_RELATIVE_MID_POINTS + 1))
                .mapToObj(i -> generateRandomizedRelativePosition()).toList());
        relation.setRelativeEndPoint(generateRandomizedRelativePosition());
        relation.setStartPoint(generateRandomizedPoint());
        relation.setEndPoint(generateRandomizedPoint());
        return relation;
    }

    private static UMLetPosition generateRandomizedUMLetPosition() {
        return new UMLetPosition(
                getRandomInt(MAX_POINT + 1),
                getRandomInt(MAX_POINT + 1),
                getRandomInt(MAX_DIMENSION) + 1,
                getRandomInt(MAX_DIMENSION) + 1
        );
    }

    private static RelativePosition generateRandomizedRelativePosition() {
        var relativePosUpperBound = MAX_RELATIVE_POSITION + 1;
        return new RelativePosition(getRandomInt(relativePosUpperBound),
                getRandomInt(relativePosUpperBound),
                getRandomInt(relativePosUpperBound),
                getRandomInt(relativePosUpperBound));
    }

    private static Point generateRandomizedPoint() {
        return new Point(getRandomInt(MAX_POINT + 1), getRandomInt(MAX_POINT + 1));
    }

    private static int getRandomInt(int upperBound) {
        if (upperBound < 0) {
            throw new IllegalArgumentException("Upper bound must be non-negative.");
        }
        return upperBound == 0 ? 0 : ThreadLocalRandom.current().nextInt(upperBound);
    }
}

