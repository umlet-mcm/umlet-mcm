package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.model.dsl.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        RelationEndpointDSLMapperImpl.class,
        RelativePositionDSLMapperImpl.class,
        NodeDSLMapperImpl.class,
        PropertiesDSLMapperImpl.class,
        PanelAttributesDSLMapperImpl.class,
        CoordinatesDSLMapperImpl.class,
        RelationDSLMapperImpl.class,
})
class RelationDSLMapperImplTest {

    @Autowired
    private RelationDSLMapperImpl mapper;

    @Test
    void testToDSL() throws DSLException {
        Relation relation = new Relation();
        relation.setId("relationId");
        relation.setDescription("Test Relation");
        relation.setMcmAttributes(Map.of("key1", "value1", "key2", "value2"));
        relation.setRelativeMidPoints(Collections.emptyList());
        relation.setRelativeStartPoint(new RelativePosition(10, 20, 30, 40));
        relation.setRelativeEndPoint(new RelativePosition(10, 20, 30, 40));
        relation.setType("TestType");
        relation.setUmletPosition(new UMLetPosition());
        relation.setMcmType("TestMcmType");
        relation.setType("line");

        Node source = new Node();
        source.setId("sourceId");
        source.setDescription("Source Node");

        RelationDSL result = mapper.toDSL(relation, source);

        assertNotNull(result);
        assertEquals(relation.getId(), result.getId());
        assertEquals(relation.getDescription(), result.getText());
        assertNotNull(result.getProperties());
        assertEquals(2, result.getProperties().size());
        assertEquals(relation.getMcmType(), result.getMcmType());
        assertEquals(relation.getType(), result.getElementType());

        assertNotNull(result.getMetadata());
        assertNotNull(result.getMetadata().getCoordinates());
        assertNotNull(result.getMetadata().getCoordinates());
        assertNotNull(result.getMetadata().getPositions());
        assertNotNull(result.getMetadata().getPositions().getRelativeStartPoint());
        assertNotNull(result.getMetadata().getPositions().getRelativeEndPoint());
        assertNull(result.getMetadata().getAdditionalAttributes());

    }

    @Test
    void testToDSL_NullInput() throws DSLException {
        assertNull(mapper.toDSL(null, null));
    }

    @Test
    void testFromDSL() throws DSLException {
        RelationDSL relationDSL = new RelationDSL();
        relationDSL.setId("relationId");
        relationDSL.setText("Test Relation");
        relationDSL.setMcmType("TestType");
        relationDSL.setElementType("line");

        MetadataDSL metadata = new MetadataDSL();
        CoordinatesDSL coordinates = new CoordinatesDSL(10, 20, 30, 40);
        metadata.setCoordinates(coordinates);

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
        assertEquals(relationDSL.getText(), result.getDescription());
        assertEquals(target, result.getTarget());
        assertEquals(relationDSL.getMcmType(), result.getMcmType());
        assertEquals(relationDSL.getElementType(), result.getType());

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
        assertNotNull(result.getRelativeEndpoint());
    }

    @Test
    void testFromDSL_NullInput() throws DSLException {
        assertNull(mapper.fromDSL(null, null));
    }

    @Test
    void testFromDSL_WithNullMetadata() throws DSLException {
        RelationDSL relationDSL = new RelationDSL();
        relationDSL.setId("relationId");
        relationDSL.setText("Test Relation");
        relationDSL.setMetadata(new MetadataDSL());

        Node target = new Node();
        target.setId("targetId");
        target.setDescription("Target Node");

        assertThrows(DSLException.class, () -> mapper.fromDSL(relationDSL, target));
    }
}
