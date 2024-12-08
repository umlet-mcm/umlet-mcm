package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.PropertyDSL;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelDSLMapperTest {

    private final ModelDSLMapper modelDSLMapper = new ModelDSLMapperImpl(
            new PropertiesDSLMapperImpl()
    );

    @Test
    public void testToModelDSL() {
        Model model = new Model();
        model.setId("id");
        model.setTitle("title");
        model.setDescription("description");
        model.setTags(List.of("tag1", "tag2"));

        LinkedHashMap<String, Object> mcmAttributes = new LinkedHashMap<>(Map.of(
                "key1", "value", "key2", "value2", "key3", 3
        ));
        model.setMcmAttributes(mcmAttributes);

        model.setOriginalText("original text");
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
                new PropertyDSL("key1", "value1"),
                new PropertyDSL("key2", "value2"),
                new PropertyDSL("key3", "3")
        ));
        modelDSL.setMetadata(new MetadataDSL());
        modelDSL.getMetadata().setOriginalText("original text");
        modelDSL.setZoomLevel(10);

        Model model = modelDSLMapper.fromDSL(modelDSL);

        testEquals(model, modelDSL);
    }

    private static void testEquals(Model model, ModelDSL modelDSL) {
        assertEquals(model.getId(), modelDSL.getId());
        assertEquals(model.getTitle(), modelDSL.getTitle());
        assertEquals(model.getDescription(), modelDSL.getDescription());
        assertEquals(model.getTags(), modelDSL.getTags());
        testPropertiesEqual(model.getMcmAttributes(), modelDSL.getProperties());
        assertEquals(model.getOriginalText(), modelDSL.getMetadata().getOriginalText());
        assertEquals(model.getZoomLevel(), modelDSL.getZoomLevel());
    }

    private static void testPropertiesEqual(Map<String, Object> mcmAttributes, Collection<PropertyDSL> properties) {
        assertEquals(properties.size(), mcmAttributes.size());
        for (PropertyDSL property : properties) {
            assertEquals(mcmAttributes.get(property.getKey()).toString(), property.getValue());
        }
    }
}
