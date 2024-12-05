package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.core.model.attributes.ElementAttributes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = MapperTestConfig.class)
public class McmAttributesMapperTest {

    private McmAttributesMapper mapper;

    @Test
    public void testPopulateFieldsBaseAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(AttributeKeys.ID, "123");
        attributes.put(AttributeKeys.TAGS, List.of("tag1", "tag2"));

        BaseAttributes base = new Node();
        McmAttributesMapper.populateFields(attributes, base);

        assertEquals("123", base.getId());
        assertEquals(List.of("tag1", "tag2"), base.getTags());
        assertFalse(base.getMcmAttributes().containsKey(AttributeKeys.ID));
        assertFalse(base.getMcmAttributes().containsKey(AttributeKeys.TAGS));
    }

    @Test
    public void testPopulateFieldsElementAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put(AttributeKeys.ID, "456");
        attributes.put(AttributeKeys.TAGS, List.of("tagA", "tagB"));
        attributes.put(AttributeKeys.MODEL, "modelX");
        attributes.put(AttributeKeys.PPR_TYPE, "typeY");

        ElementAttributes element = new Node();
        McmAttributesMapper.populateFields(attributes, element);

        assertEquals("456", element.getId());
        assertEquals(List.of("tagA", "tagB"), element.getTags());
        assertEquals("modelX", element.getMcmModel());
        assertEquals("typeY", element.getPprType());
        assertFalse(element.getMcmAttributes().containsKey(AttributeKeys.ID));
        assertFalse(element.getMcmAttributes().containsKey(AttributeKeys.TAGS));
        assertFalse(element.getMcmAttributes().containsKey(AttributeKeys.MODEL));
        assertFalse(element.getMcmAttributes().containsKey(AttributeKeys.PPR_TYPE));
    }

    @Test
    public void testMergeAttributesBaseAttributes() {
        BaseAttributes base = new Node();
        LinkedHashMap<String, Object> mcmAttributes = new LinkedHashMap<>();
        mcmAttributes.put(AttributeKeys.ID, "789");
        mcmAttributes.put(AttributeKeys.TAGS, List.of("tag1", "tag2"));
        base.setMcmAttributes(mcmAttributes);

        LinkedHashMap<String, Object> result = McmAttributesMapper.mergeAttributes(base);

        assertEquals("789", result.get(AttributeKeys.ID));
        assertEquals(List.of("tag1", "tag2"), result.get(AttributeKeys.TAGS));
    }

    @Test
    public void testMergeAttributesElementAttributes() {
        ElementAttributes element = new Node();
        LinkedHashMap<String, Object> mcmAttributes = new LinkedHashMap<>();
        mcmAttributes.put(AttributeKeys.ID, "012");
        mcmAttributes.put(AttributeKeys.TAGS, List.of("tagX", "tagY"));
        mcmAttributes.put(AttributeKeys.MODEL, "modelZ");
        mcmAttributes.put(AttributeKeys.PPR_TYPE, "typeW");
        element.setMcmAttributes(mcmAttributes);

        LinkedHashMap<String, Object> result = McmAttributesMapper.mergeAttributes(element);

        assertEquals("012", result.get(AttributeKeys.ID));
        assertEquals(List.of("tagX", "tagY"), result.get(AttributeKeys.TAGS));
        assertEquals("modelZ", result.get(AttributeKeys.MODEL));
        assertEquals("typeW", result.get(AttributeKeys.PPR_TYPE));
    }
}
