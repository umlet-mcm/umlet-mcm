package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        RelativePositionDSLMapperImpl.class,
        NodeDSLMapperImpl.class,
        PropertiesDSLMapperImpl.class,
        PanelAttributesDSLMapperImpl.class,
        CoordinatesDSLMapperImpl.class,
        RelationDSLMapperImpl.class,
        RelationEndpointDSLMapperImpl.class,
})
public class RelationDSLMapperImplTest {

    @Autowired
    private RelationDSLMapperImpl mapper;

    @Test
    void testToDSL() {
        Relation relation = new Relation();
        relation.setId("relationId");
        relation.setDescription("Test Relation");
        relation.setMcmAttributes(new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")));
        relation.setRelativeMidPoints(Collections.emptyList());
        relation.setRelativeStartPoint(new RelativePosition(10, 20, 30, 40));
        relation.setRelativeEndPoint(new RelativePosition(10, 20, 30, 40));
        relation.setType("TestType");
        relation.setUmletPosition(new UMLetPosition(10, 20, 30, 40));
        relation.setPprType("TestMcmType");
        relation.setType("line");
        relation.setOriginalText("Original Text");
        relation.setMcmModel("MCM Model");
        relation.setMcmModelId("6c1d704c-7535-4020-a8a4-8ef39b9cd22e");
        relation.setUmletAttributes(new LinkedHashMap<>(Map.of("key1", "value1")));

        Node source = new Node();
        source.setId("sourceId");
        source.setDescription("Source Node");

        RelationDSL result = mapper.toDSL(relation, source);

        assertNotNull(result);
        assertEquals(relation.getId(), result.getId());
        assertEquals(relation.getDescription(), result.getDescription());
        assertNotNull(result.getProperties());
        assertEquals(2, result.getProperties().size());
        assertEquals(relation.getPprType(), result.getPprType());
        assertEquals(relation.getType(), result.getElementType());
        assertEquals(relation.getOriginalText(), result.getMetadata().getOriginalText());
        assertEquals(relation.getMcmModel(), result.getMcmModel());
        assertEquals(relation.getMcmModelId(), result.getMcmModelId());

        assertNotNull(result.getMetadata());
        assertNotNull(result.getMetadata().getCoordinates());
        assertNotNull(result.getMetadata().getCoordinates());
        assertNotNull(result.getMetadata().getPositions());
        assertNotNull(result.getMetadata().getPositions().getRelativeStartPoint());
        assertNotNull(result.getMetadata().getPositions().getRelativeEndPoint());
        assertNull(result.getMetadata().getAdditionalAttributes());
        assertNotNull(result.getMetadata().getPanelAttributes());
        assertEquals(1, result.getMetadata().getPanelAttributes().size());
        assertEquals("key1", result.getMetadata().getPanelAttributes().getFirst().getKey());
    }

    @Test
    void testToDSL_NullInput() {
        assertNull(mapper.toDSL(null, null));
    }

    @Test
    void testFromDSL() {
        RelationDSL relationDSL = new RelationDSL();
        relationDSL.setId("relationId");
        relationDSL.setTitle("Test Relation");
        relationDSL.setDescription("Description");
        relationDSL.setTags(List.of("tag1"));
        relationDSL.setPprType("TestType");
        relationDSL.setElementType("line");
        relationDSL.setMcmModel("MCM Model");
        relationDSL.setMcmModelId("6c1d704c-7535-4020-a8a4-8ef39b9cd22e");

        MetadataDSL metadata = new MetadataDSL();
        metadata.setCoordinates(new CoordinatesDSL(10, 20, 30, 40));
        metadata.setPanelAttributes(List.of(new PanelAttributeDSL("key1", "value1")));
        metadata.setOriginalText("Original Text");

        PositionsDSL positions = new PositionsDSL();
        positions.setRelativeStartPoint(new RelativePositionDSL(10, 20, 30, 40));
        positions.setRelativeMidPoints(Collections.emptyList());
        positions.setRelativeEndPoint(new RelativePositionDSL(50, 60, 70, 80));
        metadata.setPositions(positions);
        relationDSL.setMetadata(metadata);

        List<PropertyDSL> properties = List.of(
                new PropertyDSL("key1", "value1"),
                new PropertyDSL("key2", "value2")
        );
        relationDSL.setProperties(properties);

        Node target = new Node();
        target.setId("targetId");
        target.setDescription("Target Node");

        Relation result = mapper.fromDSL(relationDSL, target);

        assertNotNull(result);
        assertEquals(relationDSL.getId(), result.getId());
        assertEquals(relationDSL.getTitle(), result.getTitle());
        assertEquals(relationDSL.getDescription(), result.getDescription());
        assertEquals(target, result.getTarget());
        assertEquals(relationDSL.getPprType(), result.getPprType());
        assertEquals(relationDSL.getElementType(), result.getType());
        assertEquals(relationDSL.getMcmModel(), result.getMcmModel());
        assertEquals(relationDSL.getMcmModelId(), result.getMcmModelId());

        assertNotNull(result.getTags());
        assertEquals(relationDSL.getTags(), result.getTags());

        assertNotNull(result.getUmletAttributes());
        assertEquals(metadata.getPanelAttributes().size(), result.getUmletAttributes().size());
        assertEquals(metadata.getOriginalText(), result.getOriginalText());

        assertNotNull(result.getMcmAttributes());
        assertEquals(relationDSL.getProperties().size(), result.getMcmAttributes().size());
        assertEquals(relationDSL.getProperties().get(0).getValue(), result.getMcmAttributes().get(relationDSL.getProperties().get(0).getKey()));
        assertEquals(relationDSL.getProperties().get(1).getValue(), result.getMcmAttributes().get(relationDSL.getProperties().get(1).getKey()));

        assertNotNull(result.getUmletPosition());
        assertEquals(metadata.getCoordinates().getX(), result.getUmletPosition().getX());
        assertEquals(metadata.getCoordinates().getY(), result.getUmletPosition().getY());

        assertNotNull(result.getStartPoint());
        assertNotNull(result.getEndPoint());
        assertTrue(result.getRelativeMidPoints().isEmpty());
        assertNotNull(result.getRelativeStartPoint());
        assertNotNull(result.getRelativeEndPoint());
    }

    @Test
    void testFromDSL_NullInput() {
        assertNull(mapper.fromDSL(null, null));
    }

    @Test
    void testFromDSL_WithNullMetadata() {
        RelationDSL relationDSL = new RelationDSL();
        relationDSL.setId("relationId");
        relationDSL.setTitle("Test Relation");
        relationDSL.setMetadata(new MetadataDSL());

        Node target = new Node();
        target.setId("targetId");
        target.setDescription("Target Node");

        Relation relation = mapper.fromDSL(relationDSL, target);

        assertNotNull(relation);
    }
}
