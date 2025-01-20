package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.dsl.KeyValueDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelDSLMapperTest {

    private final ModelDSLMapper modelDSLMapper = new ModelDSLMapperImpl(
            new KeyValuesDSLMapperImpl()
    );

    @Test
    public void testToModelDSL() {
        Model model = new Model();
        model.setId("id");
        model.setTitle("title");
        model.setDescription("description");
        model.setTags(List.of("tag1", "tag2"));
        model.setMcmAttributesInlineComments(new LinkedHashMap<>(Map.of(
                "key1", "comment1",
                "key2", "comment2"
        )));

        LinkedHashMap<String, Object> mcmAttributes = new LinkedHashMap<>(Map.of(
                "key1", "value", "key2", "value2", "key3", 3
        ));
        model.setMcmAttributes(mcmAttributes);

        model.setZoomLevel(10);

        ModelDSL modelDSL = modelDSLMapper.toDSL(model);

        testEquals(model, modelDSL);
    }

    @Test
    public void testToModel() {
        ModelDSL modelDSL = new ModelDSL();
        modelDSL.setId("id");
        modelDSL.setTitle("title");
        modelDSL.setDescription("description");
        modelDSL.setTags(List.of("tag1", "tag2"));
        modelDSL.setProperties(List.of(
                new KeyValueDSL("key1", "value1"),
                new KeyValueDSL("key2", "value2"),
                new KeyValueDSL("key3", "3")
        ));
        modelDSL.setPropertiesInlineComments(List.of(new KeyValueDSL("key", "comment1")));
        modelDSL.setMetadata(new MetadataDSL());
        modelDSL.setZoomLevel(10);

        Model model = modelDSLMapper.fromDSL(modelDSL);

        testEquals(model, modelDSL);
    }

    private static void testEquals(Model model, ModelDSL modelDSL) {
        assertEquals(model.getId(), modelDSL.getId());
        assertEquals(model.getTitle(), modelDSL.getTitle());
        assertEquals(model.getDescription(), modelDSL.getDescription());
        assertEquals(model.getTags(), modelDSL.getTags());
        testKeyValuesEqual(model.getMcmAttributes(), modelDSL.getProperties());
        assertEquals(model.getMcmAttributesInlineComments().size(), modelDSL.getPropertiesInlineComments().size());
        assertEquals(model.getZoomLevel(), modelDSL.getZoomLevel());
    }

    private static void testKeyValuesEqual(Map<String, Object> mcmAttributes, Collection<KeyValueDSL> properties) {
        assertEquals(properties.size(), mcmAttributes.size());
        for (KeyValueDSL property : properties) {
            assertEquals(mcmAttributes.get(property.getKey()).toString(), property.getValue());
        }
    }
}
