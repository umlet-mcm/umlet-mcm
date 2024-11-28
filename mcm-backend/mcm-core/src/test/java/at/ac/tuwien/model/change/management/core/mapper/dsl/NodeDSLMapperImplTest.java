package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void testToDSLWithValidInput() {
        Node node = new Node();
        node.setId("node1");
        node.setElementType("type1");
        node.setPprType("mcmType1");
        node.setDescription("A node");
        node.setUmletPosition(new UMLetPosition(10, 20, 30, 40));
        node.setMcmAttributes(new LinkedHashMap<>(Map.of("key1", "value1")));
        node.setUmletAttributes(new LinkedHashMap<>(Map.of("key2", "value2")));
        node.setGeneratedAttributes(List.of(10));
        node.setMcmModel("mcmModel1");

        NodeDSL result = nodeDSLMapper.toDSL(node);

        assertNotNull(result);
        assertEquals(node.getId(), result.getId());
        assertEquals(node.getElementType(), result.getElementType());
        assertEquals(node.getPprType(), result.getPprType());
        assertEquals(node.getDescription(), result.getDescription());
        assertEquals(node.getMcmModel(), result.getMcmModel());
        assertNotNull(result.getMetadata().getPanelAttributes());
        assertEquals(node.getUmletAttributes().size(), result.getMetadata().getPanelAttributes().size());
        assertEquals(node.getGeneratedAttributes().size(), result.getMetadata().getAdditionalAttributes().size());
        assertNotNull(result.getProperties());
        assertEquals(node.getMcmAttributes().size(), result.getProperties().size());
    }

    @Test
    void testFromDSLWithValidInput() {
        NodeDSL nodeDSL = new NodeDSL();
        nodeDSL.setId("node1");
        nodeDSL.setElementType("type1");
        nodeDSL.setTitle("A node");
        nodeDSL.setPprType("type1");
        nodeDSL.setDescription("Description");
        nodeDSL.setProperties(List.of(new PropertyDSL("kProp1", "vProp1")));
        nodeDSL.setTags(List.of("tag1"));
        nodeDSL.setMcmModel("mcmModel1");

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setCoordinates(new CoordinatesDSL());
        metadataDSL.setPanelAttributes(List.of(new PanelAttributeDSL("kPan1", "vPan1")));
        metadataDSL.setAdditionalAttributes(List.of(10));
        nodeDSL.setMetadata(metadataDSL);

        Node result = nodeDSLMapper.fromDSL(nodeDSL);

        assertNotNull(result);
        assertEquals(nodeDSL.getId(), result.getId());
        assertEquals(nodeDSL.getElementType(), result.getElementType());
        assertEquals(nodeDSL.getTitle(), result.getTitle());
        assertEquals(nodeDSL.getPprType(), result.getPprType());
        assertEquals(nodeDSL.getDescription(), result.getDescription());
        assertEquals(nodeDSL.getMcmModel(), result.getMcmModel());
        assertNotNull(result.getUmletPosition());
        assertEquals(nodeDSL.getTags(), result.getTags());
        assertEquals(nodeDSL.getProperties().size(), result.getMcmAttributes().size());
        assertEquals(nodeDSL.getMetadata().getPanelAttributes().size(), result.getUmletAttributes().size());
        assertEquals(nodeDSL.getMetadata().getAdditionalAttributes().size(), result.getGeneratedAttributes().size());
    }
}
