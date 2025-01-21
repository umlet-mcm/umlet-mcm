package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.model.utils.PositionUtils;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.git.repository.VersionControlRepository;
import at.ac.tuwien.model.change.management.testutil.MockConfigurationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ModelServiceTest {
    private ConfigurationRepository configurationRepository;
    private AutoCloseable annotations;

    @Mock
    private VersionControlRepository versionControlRepository;

    private ConfigurationService configurationService;
    private ModelService modelService;

    @Mock
    private GraphDBService graphDBService;

    @BeforeEach
    public void setup() {
        configurationRepository = new MockConfigurationRepository();
        annotations = MockitoAnnotations.openMocks(this);
        configurationService = new ConfigurationServiceImpl(configurationRepository, versionControlRepository, graphDBService);
        modelService = new ModelServiceImpl(configurationService);
    }

    @AfterEach
    public void teardown() throws Exception {
        annotations.close();
    }

    @Test
    public void testDeleteModelValidID() {
        var configuration = new Configuration();
        configuration.setName("test");
        var model = new Model();
        model.setId("id");
        configuration.getModels().add(model);
        when(versionControlRepository.getCurrentVersion(anyString())).thenAnswer(invocation ->
                Optional.of(findVersionByName(invocation.getArgument(0))));

        configurationService.createConfiguration(configuration);
        Assertions.assertDoesNotThrow(() -> modelService.deleteModel(model.getId()));

        var emptyConf = configurationService.getConfigurationByName(configuration.getName());
        Assertions.assertEquals(0, emptyConf.getModels().size());
    }

    @Test
    public void testDeleteModelInvalidID() {
        int confCount = configurationService.getAllConfigurations().size();
        Assertions.assertThrows(ModelNotFoundException.class, () -> modelService.deleteModel("invalid id"));
        Assertions.assertEquals(confCount, configurationService.getAllConfigurations().size());
    }

    @Test
    public void testAlignModels_emptyList_shouldReturnEmptyList() {
        Assertions.assertTrue(modelService.alignModels(List.of()).isEmpty());
    }

    @Test
    public void testAlignModels_multipleModelsOverlap_shouldReturnAlignedPositions() {
        Model model1 = createModel(
                "m1",
                new UMLetPosition(110, 210, 50, 50),
                new UMLetPosition(110, 210, 50, 50)
        );
        Model model2 = createModel(
                "m2",
                new UMLetPosition(100, 210, 50, 50),
                new UMLetPosition(100, 210, 50, 50)
        );

        var alignedModels = modelService.alignModels(List.of(model1, model2));

        var m1AlignedNode = getModelAlignedNode(alignedModels, "m1");
        var m2AlignedNode = getModelAlignedNode(alignedModels, "m2");

        assertPositionsEqual(
                model1.getNodes().iterator().next().getUmletPosition(),
                m1AlignedNode.getUmletPosition()
        );
        assertPositionsEqual(
                model1.getNodes().iterator().next().getRelations().iterator().next().getUmletPosition(),
                m1AlignedNode.getRelations().iterator().next().getUmletPosition()
        );

        var expectedX = 110 + 50 + PositionUtils.MODEL_PADDING;
        var node2 = model2.getNodes().iterator().next();
        Assertions.assertEquals(expectedX, m2AlignedNode.getUmletPosition().getX());
        Assertions.assertEquals(node2.getUmletPosition().getY(), m2AlignedNode.getUmletPosition().getY());
        Assertions.assertEquals(node2.getUmletPosition().getWidth(), m2AlignedNode.getUmletPosition().getWidth());
        Assertions.assertEquals(node2.getUmletPosition().getHeight(), m2AlignedNode.getUmletPosition().getHeight());

        var relation2 = node2.getRelations().iterator().next();
        Assertions.assertEquals(expectedX, m2AlignedNode.getRelations().iterator().next().getUmletPosition().getX());
        Assertions.assertEquals(relation2.getUmletPosition().getY(), m2AlignedNode.getRelations().iterator().next().getUmletPosition().getY());
        Assertions.assertEquals(relation2.getUmletPosition().getWidth(), m2AlignedNode.getRelations().iterator().next().getUmletPosition().getWidth());
        Assertions.assertEquals(relation2.getUmletPosition().getHeight(), m2AlignedNode.getRelations().iterator().next().getUmletPosition().getHeight());
    }

    @Test
    public void testAlignModels_multipleModelsNoOverlap_shouldReturnInitialPositions() {
        Model model1 = createModel(
                "m1",
                new UMLetPosition(100, 200, 50, 50),
                new UMLetPosition(100, 200, 50, 50)
        );
        Model model2 = createModel(
                "m2",
                new UMLetPosition(400, 200, 50, 50),
                new UMLetPosition(400, 200, 50, 50)
        );

        var alignedModels = modelService.alignModels(List.of(model1, model2));

        var m1AlignedNode = getModelAlignedNode(alignedModels, "m1");
        var m2AlignedNode = getModelAlignedNode(alignedModels, "m2");

        assertPositionsEqual(
                model1.getNodes().iterator().next().getUmletPosition(),
                m1AlignedNode.getUmletPosition()
        );
        assertPositionsEqual(
                model1.getNodes().iterator().next().getRelations().iterator().next().getUmletPosition(),
                m1AlignedNode.getRelations().iterator().next().getUmletPosition()
        );
        assertPositionsEqual(
                model2.getNodes().iterator().next().getUmletPosition(),
                m2AlignedNode.getUmletPosition()
        );
        assertPositionsEqual(
                model2.getNodes().iterator().next().getRelations().iterator().next().getUmletPosition(),
                m2AlignedNode.getRelations().iterator().next().getUmletPosition()
        );
    }

    private void assertPositionsEqual(UMLetPosition expected, UMLetPosition actual) {
        Assertions.assertEquals(expected.getX(), actual.getX());
        Assertions.assertEquals(expected.getY(), actual.getY());
        Assertions.assertEquals(expected.getWidth(), actual.getWidth());
        Assertions.assertEquals(expected.getHeight(), actual.getHeight());
    }

    private Model createModel(String description, UMLetPosition nodePosition, UMLetPosition relationPosition) {
        Relation relation = new Relation();
        relation.setType("relation");
        relation.setUmletPosition(relationPosition);
        relation.setRelativeStartPoint(new RelativePosition(10, 10, relationPosition.getX(), relationPosition.getY()));
        relation.setRelativeEndPoint(new RelativePosition(150, 150, relationPosition.getWidth(), relationPosition.getHeight()));

        Node node = new Node();
        node.setElementType("node");
        node.setUmletPosition(nodePosition);
        node.setRelations(Set.of(relation));

        Model model = new Model();
        model.setDescription(description);
        model.setNodes(Set.of(node));

        return model;
    }

    private Node getModelAlignedNode(List<Model> alignedModels, String modelDescription) {
        return alignedModels.stream()
                .filter(m -> m.getDescription().equals(modelDescription))
                .findFirst()
                .get()
                .getNodes()
                .iterator()
                .next();
    }

    private String findVersionByName(String name) {
        return configurationRepository.findCurrentVersionOfConfigurationByName(name).orElseThrow().getVersionHash();
    }
}
