package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = MapperTestConfig.class)
public class ElementUxfMapperTest {

    private ElementUxfMapper mapper = Mappers.getMapper(ElementUxfMapper.class);

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

        Node node = mapper.toNode(element, 10);

        assertEquals(element.getElementType(), node.getElementType());
        assertEquals(1, node.getMcmAttributes().size());
        assertEquals(mcmAttributes.get("misc"), node.getMcmAttributes().get("misc"));
        assertEquals(mcmAttributes.get("id"), node.getId());
        assertEquals(mcmAttributes.get("tags"), node.getTags());
        assertEquals(mcmAttributes.get("model"), node.getMcmModel());
        assertEquals(mcmAttributes.get("pprType"), node.getPprType());
        assertEquals(element.getAttributes().getUmletAttributes(), node.getUmletAttributes());
        assertEquals(Map.of("misc", "misc"), node.getMcmAttributes());
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
        node.setMcmModelId("mID");
        node.setPprType("pprType");
        node.setDescription("Test Description");
        node.setTitle("Title");
        node.setMcmAttributes(new LinkedHashMap<>(Map.of("misc", "misc")));
        node.setUmletAttributes(new LinkedHashMap<>(Map.of("key", "value")));
        node.setGeneratedAttributes(List.of(1, 2, 3));
        node.setUmletPosition(new UMLetPosition(1, 2, 3, 4));

        ElementUxf element = mapper.fromNode(node, 10);

        assertEquals(node.getElementType(), element.getElementType());

        assertEquals(6, element.getAttributes().getMcmAttributes().size());
        assertEquals(node.getId(), element.getAttributes().getMcmAttributes().get(AttributeKeys.ID));
        assertEquals(node.getTags(), element.getAttributes().getMcmAttributes().get(AttributeKeys.TAGS));
        assertEquals(node.getMcmModel(), element.getAttributes().getMcmAttributes().get(AttributeKeys.MODEL));
        assertEquals(node.getMcmModelId(), element.getAttributes().getMcmAttributes().get(AttributeKeys.MODEL_ID));
        assertEquals(node.getPprType(), element.getAttributes().getMcmAttributes().get(AttributeKeys.PPR_TYPE));
        assertEquals(node.getMcmAttributes().get("misc"), element.getAttributes().getMcmAttributes().get("misc"));


        assertEquals(node.getUmletAttributes(), element.getAttributes().getUmletAttributes());
        assertEquals(node.getGeneratedAttributes(), element.getGeneratedAttributes());
        assertNotNull(element.getUmletPosition());
    }
}
