package at.ac.tuwien.model.change.management.server.testutil;

import at.ac.tuwien.model.change.management.server.dto.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.lang.Nullable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class DtoGen {
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

    private DtoGen() {
    }

    private enum ElementType {
        NODE,
        RELATION
    }

    public static ConfigurationDTO generateRandomizedConfigurationDTO(
            String name,
            int numberModels,
            int numberNodesPerModel,
            int numberRelationsPerNode) {
        return generateRandomizedConfigurationDTO(
                name,
                null,
                numberModels,
                numberNodesPerModel,
                numberRelationsPerNode);
    }

    public static ConfigurationDTO generateRandomizedConfigurationDTO(
            String name,
            @Nullable String version,
            int numberModels,
            int numberNodesPerModel,
            int numberRelationsPerNode) {
        return generateRandomizedConfigurationDTO(
                name,
                version,
                numberModels,
                numberModels,
                numberNodesPerModel,
                numberNodesPerModel,
                numberRelationsPerNode,
                numberRelationsPerNode
        );
    }

    public static ConfigurationDTO generateRandomizedConfigurationDTO(
            String name,
            String version,
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
        int upperBoundModels = maxNumberModels - minNumberModels + 1;
        int numModels = getRandomInt(upperBoundModels) + minNumberModels;
        Set<ModelDTO> models = new HashSet<>();
        for (int i = 0; i < numModels; i++) {
            models.add(generateRandomizedModelDTO(
                    minNumberNodesPerModel,
                    maxNumberNodesPerModel,
                    minNumberRelationsPerNode,
                    maxNumberRelationsPerNode
            ));
        }
        return new ConfigurationDTO(
                name,
                version,
                models
        );
    }

    public static ModelDTO generateRandomizedModelDTO(int numberNodes, int numberRelationsPerNode) {
        return generateRandomizedModelDTO(numberNodes, numberNodes, numberRelationsPerNode, numberRelationsPerNode);
    }

    public static ModelDTO generateRandomizedModelDTO(
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
        int numNodes = getRandomInt(upperBoundNodes) + minNumberNodes;
        Set<NodeDTO> nodes = new HashSet<>();
        for (int i = 0; i < numNodes; i++) {
            nodes.add(generateRandomizedNodeDTO());
        }
        for (NodeDTO node : nodes) {
            int numRelations = getRandomInt(upperBoundRelations) + minNumberRelationsPerNode;
            List<NodeDTO> availableTargets = nodes.stream().filter(n -> !n.equals(node)).toList();
            for (int i = 0; i < numRelations; i++) {
                if (!availableTargets.isEmpty()) {
                    node.relations().add(generateRandomizedRelationDTO(availableTargets));
                }
            }
        }
        return new ModelDTO(
                nodes,
                null,
                IntStream.range(0, getRandomInt(MAX_TAG_NUMBER + 1))
                        .mapToObj(i -> RandomStringUtils.randomAlphabetic(MAX_TAG_LENGTH))
                        .toList(),
                RandomStringUtils.randomAlphabetic(MAX_DESCRIPTION_LENGTH),
                RandomStringUtils.randomAlphabetic(MAX_TITLE_LENGTH),
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                getRandomInt(10)
        );
    }

    public static NodeDTO generateRandomizedNodeDTO() {
        return new NodeDTO(
                ElementType.NODE.toString(),
                IntStream.range(0, getRandomInt(MAX_GENERATED_ATTRIBUTE_NUMBER + 1))
                        .mapToObj(i -> getRandomInt(MAX_GENERATED_ATTRIBUTE_VALUE + 1))
                        .toList(),
                generateRandomizedUMLetPositionDTO(),
                new HashSet<>(),
                RandomStringUtils.randomAlphanumeric(40).toLowerCase(),
                IntStream.range(0, getRandomInt(MAX_TAG_NUMBER + 1))
                        .mapToObj(i -> RandomStringUtils.randomAlphabetic(MAX_TAG_LENGTH))
                        .toList(),
                RandomStringUtils.randomAlphabetic(MAX_DESCRIPTION_LENGTH),
                RandomStringUtils.randomAlphabetic(MAX_TITLE_LENGTH),
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                RandomStringUtils.randomAlphabetic(MAX_TITLE_LENGTH),
                null,
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                RandomStringUtils.randomAlphabetic(MAX_PPR_TYPE_LENGTH)
        );
    }

    public static RelationDTO generateRandomizedRelationDTO(List<NodeDTO> availableTargets) {
        int randomTargetIndex = getRandomInt(availableTargets.size());
        return new RelationDTO(
                ElementType.RELATION.toString(),
                availableTargets.get(randomTargetIndex).id(),
                generateRandomizedUMLetPositionDTO(),
                generateRandomizedRelativePositionDTO(),
                IntStream.range(0, getRandomInt(MAX_RELATIVE_MID_POINTS + 1))
                        .mapToObj(i -> generateRandomizedRelativePositionDTO())
                        .toList(),
                generateRandomizedRelativePositionDTO(),
                generateRandomizedPointDTO(),
                generateRandomizedPointDTO(),
                RandomStringUtils.randomAlphanumeric(40).toLowerCase(),
                IntStream.range(0, getRandomInt(MAX_TAG_NUMBER + 1))
                        .mapToObj(i -> RandomStringUtils.randomAlphabetic(MAX_TAG_LENGTH))
                        .toList(),
                RandomStringUtils.randomAlphabetic(MAX_DESCRIPTION_LENGTH),
                RandomStringUtils.randomAlphabetic(MAX_TITLE_LENGTH),
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                RandomStringUtils.randomAlphabetic(MAX_TITLE_LENGTH),
                null,
                IntStream.range(0, getRandomInt(MAX_MCM_ATTRIBUTE_NUMBER + 1)).boxed().collect(Collectors.toMap(
                        k -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_KEY_LENGTH),
                        v -> RandomStringUtils.randomAlphabetic(MAX_MCM_ATTRIBUTE_VALUE_LENGTH),
                        (ex, rep) -> rep,
                        LinkedHashMap::new
                )),
                RandomStringUtils.randomAlphabetic(MAX_PPR_TYPE_LENGTH)
        );
    }

    private static UMLetPositionDTO generateRandomizedUMLetPositionDTO() {
        return new UMLetPositionDTO(
                getRandomInt(MAX_POINT + 1),
                getRandomInt(MAX_POINT + 1),
                getRandomInt(MAX_DIMENSION) + 1,
                getRandomInt(MAX_DIMENSION) + 1
        );
    }

    private static RelativePositionDTO generateRandomizedRelativePositionDTO() {
        int bound = MAX_RELATIVE_POSITION + 1;
        return new RelativePositionDTO(
                getRandomInt(bound),
                getRandomInt(bound),
                getRandomInt(bound),
                getRandomInt(bound)
        );
    }

    private static PointDTO generateRandomizedPointDTO() {
        return new PointDTO(getRandomInt(MAX_POINT + 1), getRandomInt(MAX_POINT + 1));
    }

    private static int getRandomInt(int upperBound) {
        if (upperBound < 0) {
            throw new IllegalArgumentException("Upper bound must be non-negative.");
        }
        return upperBound == 0 ? 0 : ThreadLocalRandom.current().nextInt(upperBound);
    }
}
