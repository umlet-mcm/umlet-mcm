package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.dsl.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        NodeDSLMapperImpl.class,
        PropertiesDSLMapperImpl.class,
        PanelAttributesDSLMapperImpl.class,
        CoordinatesDSLMapperImpl.class
})
public class NodeDSLMapperImplTest {

    @Autowired
    private NodeDSLMapper nodeDSLMapper;

    @Test
    void testToDSLWithValidInput() throws DSLException {
        Node node = new Node();
        node.setId("node1");
        node.setElementType("type1");
        node.setMcmType("mcmType1");
        node.setDescription("A node");
        node.setUmletPosition(new UMLetPosition());
        node.setMcmAttributes(Map.of("key1", "value1"));
        node.setUmletAttributes(Map.of("key2", "value2"));
        node.setGeneratedAttributes(List.of("additional1"));

        NodeDSL result = nodeDSLMapper.toDSL(node);

        assertNotNull(result);
        assertEquals(node.getId(), result.getId());
        assertEquals(node.getElementType(), result.getElementType());
        assertEquals(node.getMcmType(), result.getMcmType());
        assertEquals(node.getDescription(), result.getText());
        assertNotNull(result.getMetadata());
        assertNotNull(result.getMetadata().getCoordinates());
        assertNotNull(result.getMetadata().getPanelAttributes());
        assertEquals(node.getUmletAttributes().size(), result.getMetadata().getPanelAttributes().size());
        assertEquals(node.getGeneratedAttributes().size(), result.getMetadata().getAdditionalAttributes().size());
        assertNotNull(result.getProperties());
        assertEquals(node.getMcmAttributes().size(), result.getProperties().size());
    }

    @Test
    void testToDSLWithNullInput() throws DSLException {
        assertThrows(DSLException.class, () -> nodeDSLMapper.toDSL(new Node()));
    }

    @Test
    void testFromDSLWithNullInput() throws DSLException {
        assertThrows(DSLException.class, () -> nodeDSLMapper.fromDSL(new NodeDSL()));
    }

    @Test
    void testFromDSLWithValidInput() throws DSLException {
        NodeDSL nodeDSL = new NodeDSL();
        nodeDSL.setId("node1");
        nodeDSL.setElementType("type1");
        nodeDSL.setText("A node");
        nodeDSL.setMcmType("type1");

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setCoordinates(new CoordinatesDSL());
        metadataDSL.setPanelAttributes(List.of(new PanelAttributeDSL("kPan1", "vPan1")));
        metadataDSL.setAdditionalAttributes(List.of("additional1"));

        nodeDSL.setMetadata(metadataDSL);
        nodeDSL.setProperties(List.of(new PropertyDSL("kProp1", "vProp1")));
        nodeDSL.setTags(Set.of("tag1"));

        Node result = nodeDSLMapper.fromDSL(nodeDSL);

        assertNotNull(result);
        assertEquals(nodeDSL.getId(), result.getId());
        assertEquals(nodeDSL.getElementType(), result.getElementType());
        assertEquals(nodeDSL.getText(), result.getDescription());
        assertNotNull(result.getUmletPosition());
        assertEquals(nodeDSL.getTags(), result.getMcmAttributes().get(AttributeKeys.TAGS));
        // Tags lived in McmAttributes in Domain Model while in DSL they have their own field
        assertEquals(nodeDSL.getProperties().size(), result.getMcmAttributes().keySet().stream().filter(k -> !k.equals(AttributeKeys.TAGS)).toList().size());
        assertEquals(nodeDSL.getMetadata().getPanelAttributes().size(), result.getUmletAttributes().size());
        assertEquals(nodeDSL.getMetadata().getAdditionalAttributes().size(), result.getGeneratedAttributes().size());
    }
}
