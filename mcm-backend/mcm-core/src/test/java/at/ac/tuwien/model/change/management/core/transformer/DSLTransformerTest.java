package at.ac.tuwien.model.change.management.core.transformer;

import at.ac.tuwien.model.change.management.core.configuration.JaxbConfig;
import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.mapper.dsl.*;
import at.ac.tuwien.model.change.management.core.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        DSLTransformerImpl.class,
        NodeDSLMapperImpl.class,
        RelationDSLMapperImpl.class,
        PanelAttributesDSLMapperImpl.class,
        RelationEndpointDSLMapperImpl.class,
        PropertiesDSLMapperImpl.class,
        PanelAttributesDSLMapperImpl.class,
        RelativePositionDSLMapperImpl.class,
        CoordinatesDSLMapperImpl.class,
        ModelDSLMapperImpl.class,
        XMLTransformerImpl.class,
        JaxbConfig.class
})
public class DSLTransformerTest {

    @Autowired
    private DSLTransformerImpl dslService;

    @Test
    public void testNode_backAndForth() throws DSLException {
        Node node = getNewNode("1");

        String nodeDSLAsString = dslService.parseToNodeDSL(node);

        assertNotNull(nodeDSLAsString);
        assertFalse(nodeDSLAsString.isEmpty());

        Node parsedNode = dslService.parseToNodes(Set.of(nodeDSLAsString), Set.of()).iterator().next();

        assertNotNull(parsedNode);
        assertEquals(node.getId(), parsedNode.getId());
        assertEquals(node.getDescription(), parsedNode.getDescription());
        assertEquals(node.getUmletPosition().getX(), parsedNode.getUmletPosition().getX());
        assertEquals(node.getUmletPosition().getY(), parsedNode.getUmletPosition().getY());
        assertEquals(node.getUmletPosition().getWidth(), parsedNode.getUmletPosition().getWidth());
        assertEquals(node.getUmletPosition().getHeight(), parsedNode.getUmletPosition().getHeight());
        assertEquals(node.getDescription(), parsedNode.getDescription());
        assertEquals(node.getPprType(), parsedNode.getPprType());
        assertEquals(node.getRelations().size(), parsedNode.getRelations().size());
    }

    @Test
    public void testRelation_backAndForth() throws DSLException {
        Node nodeA = getNewNode("1");
        Node nodeB = getNewNode("2");

        Relation relationAB = getNewRelation("3");
        relationAB.setTarget(nodeB);
        nodeA.getRelations().add(relationAB);

        Relation relationBA = getNewRelation("4");
        relationBA.setTarget(nodeA);
        nodeB.getRelations().add(relationBA);

        Set<String> nodesDSLStrings = Set.of(dslService.parseToNodeDSL(nodeA), dslService.parseToNodeDSL(nodeB));
        Set<String> relationsDSLAsString = Set.of(dslService.parseToRelationDSL(relationAB, nodeA), dslService.parseToRelationDSL(relationBA, nodeB));

        Set<Node> nodes = dslService.parseToNodes(nodesDSLStrings, relationsDSLAsString);
        assertNotNull(nodes);
        assertEquals(2, nodes.size());

        Node parsedA = nodes.stream().filter(node -> node.getId().equals("1")).findFirst().orElse(null);
        assertNotNull(parsedA);
        assertEquals(nodeA.getId(), parsedA.getId());
        assertEquals(1, parsedA.getRelations().size());
        assertEquals(relationAB.getId(), parsedA.getRelations().iterator().next().getId());
        assertEquals(relationAB.getDescription(), parsedA.getRelations().iterator().next().getDescription());
        assertEquals(relationAB.getPprType(), parsedA.getRelations().iterator().next().getPprType());

        Node parsedB = nodes.stream().filter(node -> node.getId().equals("2")).findFirst().orElse(null);
        assertNotNull(parsedB);
        assertEquals(nodeB.getId(), parsedB.getId());
        assertEquals(1, parsedB.getRelations().size());
        assertEquals(relationBA.getId(), parsedB.getRelations().iterator().next().getId());
        assertEquals(relationBA.getDescription(), parsedB.getRelations().iterator().next().getDescription());
        assertEquals(relationBA.getPprType(), parsedB.getRelations().iterator().next().getPprType());
    }

    @Test
    public void testModel_backAndForth() throws DSLException {
        Model model = new Model();
        model.setId("1");
        model.setDescription("Model");
        model.setMcmAttributes(new LinkedHashMap<>(Map.of("key1", "val1")));

        String modelDSLAsString = dslService.parseToModelDSL(model);
        assertNotNull(modelDSLAsString);

        Model parsedModel = dslService.parseToModel(modelDSLAsString);
        assertNotNull(parsedModel);
        assertEquals(model.getId(), parsedModel.getId());
        assertEquals(model.getDescription(), parsedModel.getDescription());
        assertEquals(model.getMcmAttributes().size(), parsedModel.getMcmAttributes().size());
        assertEquals(model.getMcmAttributes().get("key1"), parsedModel.getMcmAttributes().get("key1"));
    }

    @Test
    public void testRelation_emptyTarget() throws DSLException {
        Node nodeA = getNewNode("1");
        nodeA.setRelations(Set.of(getNewRelation("3")));

        String nodeAsString = dslService.parseToNodeDSL(nodeA);
        String relationAsString = dslService.parseToRelationDSL(nodeA.getRelations().iterator().next(), nodeA);

        Set<Node> node = dslService.parseToNodes(Set.of(nodeAsString), Set.of(relationAsString));
        assertNotNull(node);
        assertEquals(1, node.size());
        assertNotNull(node.iterator().next().getRelations());
        assertEquals(1, node.iterator().next().getRelations().size());
        assertNull(node.iterator().next().getRelations().iterator().next().getTarget());
    }

    private Node getNewNode(String id) {
        Node node = new Node();
        node.setId(id);
        node.setDescription("Node");
        node.setUmletPosition(new UMLetPosition(10, 20, 30, 40));
        node.setDescription("Desc Node");
        node.setPprType("Type Node");
        return node;
    }

    private Relation getNewRelation(String id) {
        Relation relation = new Relation();
        relation.setId("1");
        relation.setDescription("Relation");
        relation.setUmletPosition(new UMLetPosition(10, 20, 30, 40));
        relation.setRelativeStartPoint(new RelativePosition(1, 2, 3, 4));
        relation.setRelativeMidPoints(List.of(new RelativePosition(5, 6, 7, 8)));
        relation.setRelativeEndPoint(new RelativePosition(9, 10, 11, 12));
        relation.setDescription("Desc Relation");
        relation.setPprType("Type Relation");

        return relation;
    }
}
