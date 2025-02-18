package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.CoordinatesDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.KeyValueDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.NodeDSL;
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
        KeyValuesDSLMapperImpl.class,
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
        node.setMcmAttributesInlineComments(new LinkedHashMap<>(Map.of("key1", "comment1")));
        node.setUmletAttributes(new LinkedHashMap<>(Map.of("key2", "value2")));
        node.setGeneratedAttributes(List.of(10));
        node.setMcmModel("mcmModel1");
        node.setMcmModelId("c4144490-b60b-4283-b8a1-51cc631c3874");

        NodeDSL result = nodeDSLMapper.toDSL(node);

        assertNotNull(result);
        assertEquals(node.getId(), result.getId());
        assertEquals(node.getElementType(), result.getElementType());
        assertEquals(node.getPprType(), result.getPprType());
        assertEquals(node.getDescription(), result.getDescription());
        assertEquals(node.getMcmModel(), result.getMcmModel());
        assertEquals(node.getMcmModelId(), result.getMcmModelId());
        assertNotNull(result.getMetadata().getPanelAttributes());
        assertEquals(node.getUmletAttributes().size(), result.getMetadata().getPanelAttributes().size());
        assertEquals(node.getGeneratedAttributes().size(), result.getMetadata().getAdditionalAttributes().size());
        assertNotNull(result.getProperties());
        assertEquals(node.getMcmAttributes().size(), result.getProperties().size());
        assertEquals(node.getMcmAttributesInlineComments().size(), result.getPropertiesInlineComments().size());
    }

    @Test
    void testFromDSLWithValidInput() {
        NodeDSL nodeDSL = new NodeDSL();
        nodeDSL.setId("node1");
        nodeDSL.setElementType("type1");
        nodeDSL.setTitle("A node");
        nodeDSL.setPprType("type1");
        nodeDSL.setDescription("Description");
        nodeDSL.setProperties(List.of(new KeyValueDSL("kProp1", "vProp1")));
        nodeDSL.setTags(List.of("tag1"));
        nodeDSL.setMcmModel("mcmModel1");
        nodeDSL.setMcmModelId("c4144490-b60b-4283-b8a1-51cc631c3874");
        nodeDSL.setPropertiesInlineComments(List.of(new KeyValueDSL("kProp1", "vProp1")));

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setCoordinates(new CoordinatesDSL());
        metadataDSL.setPanelAttributes(List.of(new KeyValueDSL("kPan1", "vPan1")));
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
        assertEquals(nodeDSL.getMcmModelId(), result.getMcmModelId());
        assertNotNull(result.getUmletPosition());
        assertEquals(nodeDSL.getTags(), result.getTags());
        assertEquals(nodeDSL.getProperties().size(), result.getMcmAttributes().size());
        assertEquals(nodeDSL.getMetadata().getPanelAttributes().size(), result.getUmletAttributes().size());
        assertEquals(nodeDSL.getMetadata().getAdditionalAttributes().size(), result.getGeneratedAttributes().size());
        assertEquals(nodeDSL.getPropertiesInlineComments().size(), result.getMcmAttributesInlineComments().size());
    }
}
