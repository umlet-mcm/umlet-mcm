package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = MapperTestConfig.class)
public class ElementUxfMapperTest {

    @Autowired
    private ElementUxfMapper mapper;

    @Test
    public void testToNode() {
        ElementUxf element = new ElementUxf();
        element.setElementType("UMLClass");

        LinkedHashMap<String, Object> mcmAttributes = new LinkedHashMap<>();
        mcmAttributes.put("id", "id");
        mcmAttributes.put("tags", List.of("tag1", "tag2"));
        mcmAttributes.put("model", "model");
        mcmAttributes.put("pprType", "pprType");
        mcmAttributes.put("misc", "misc");

        ElementAttributesUxf attributes = new ElementAttributesUxf();
        attributes.setOriginalText("Original Text");
        attributes.setDescription("Test Description");
        attributes.setUmletAttributes(new LinkedHashMap<>(Map.of("key", "value")));
        attributes.setMcmAttributes(mcmAttributes);
        element.setAttributes(attributes);

        element.setGeneratedAttributes(List.of(1, 2, 3));

        UmletPositionUxf position = new UmletPositionUxf();
        position.setX(1);
        position.setY(2);
        position.setHeight(3);
        position.setWidth(4);
        element.setUmletPosition(position);

        Node node = mapper.toNode(element,10);

        assertEquals(element.getElementType(), node.getElementType());
        assertEquals(1, node.getMcmAttributes().size());
        assertEquals(mcmAttributes.get("misc"), node.getMcmAttributes().get("misc"));
        assertEquals(mcmAttributes.get("id"), node.getId());
        assertEquals(mcmAttributes.get("tags"), node.getTags());
        assertEquals(mcmAttributes.get("model"), node.getMcmModel());
        assertEquals(mcmAttributes.get("pprType"), node.getPprType());
        assertEquals(element.getAttributes().getOriginalText(), node.getOriginalText());
        assertEquals(element.getAttributes().getDescription(), node.getDescription());
        assertEquals(element.getAttributes().getUmletAttributes(), node.getUmletAttributes());
        assertEquals(element.getAttributes().getMcmAttributes(), node.getMcmAttributes());
        assertEquals(element.getGeneratedAttributes(), node.getGeneratedAttributes());
        assertNotNull(node.getUmletPosition());
    }

    @Test
    public void testFromNode() {
        Node node = new Node();
        node.setElementType("UMLClass");
        node.setId("id");
        node.setTags(List.of("tag1", "tag2"));
        node.setMcmModel("model");
        node.setPprType("pprType");
        node.setOriginalText("Original Text");
        node.setDescription("Test Description");
        node.setTitle("Title");
        node.setMcmAttributes(new LinkedHashMap<>(Map.of("misc", "misc")));
        node.setUmletAttributes(new LinkedHashMap<>(Map.of("key", "value")));
        node.setGeneratedAttributes(List.of(1, 2, 3));
        node.setUmletPosition(new UMLetPosition(1, 2, 3, 4));

        ElementUxf element = mapper.fromNode(node, 10);

        assertEquals(node.getElementType(), element.getElementType());

        assertEquals(5, element.getAttributes().getMcmAttributes().size());
        assertEquals(node.getId(), element.getAttributes().getMcmAttributes().get("id"));
        assertEquals(node.getTags(), element.getAttributes().getMcmAttributes().get("tags"));
        assertEquals(node.getMcmModel(), element.getAttributes().getMcmAttributes().get("model"));
        assertEquals(node.getPprType(), element.getAttributes().getMcmAttributes().get("pprType"));
        assertEquals(node.getMcmAttributes().get("misc"), element.getAttributes().getMcmAttributes().get("misc"));

        assertEquals(node.getOriginalText(), element.getAttributes().getOriginalText());
        assertEquals(node.getDescription(), element.getAttributes().getDescription());

        assertEquals(node.getUmletAttributes(), element.getAttributes().getUmletAttributes());
        assertEquals(node.getGeneratedAttributes(), element.getGeneratedAttributes());
        assertNotNull(element.getUmletPosition());
    }
}
