package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MapperTestConfig.class)
public class ModelUxfMapperTest {


    private ModelUxfMapper mapper = Mappers.getMapper(ModelUxfMapper.class);

    @Test
    public void testFromModel_emptyModel() {
        Model model = new Model();
        ModelUxf result = mapper.fromModel(model);

        assertNotNull(result);
        assertNull(result.getElements());
    }

    @Test
    public void testFromModel_completeModel() {
        Model model = new Model();
        model.setZoomLevel(10);
        model.setId("123");
        model.setDescription("Description");
        model.setTitle("Title");
        model.setOriginalText("Original Text");
        model.setTags(List.of("tag1", "tag2"));
        model.setMcmAttributes(new LinkedHashMap<>(Map.of("attr1", "value1")));

        Set<Node> nodes = getNodesWithRelations();
        model.setNodes(nodes);

        ModelUxf modelUxf = mapper.fromModel(model);

        assertEquals(model.getId(), modelUxf.getAttributes().getMcmAttributes().get("id"));
        assertEquals(model.getDescription(), modelUxf.getAttributes().getDescription());
        assertEquals(model.getTags(), modelUxf.getAttributes().getMcmAttributes().get("tags"));
        assertEquals(model.getZoomLevel(), modelUxf.getZoomLevel());
        assertEquals("value1", modelUxf.getAttributes().getMcmAttributes().get("attr1"));
        assertEquals(3, modelUxf.getElements().size());

        Node src = nodes.stream()
                .filter(n -> n.getId().equals("src"))
                .findFirst().orElse(null);
        assertNotNull(src);

        Relation relation = src.getRelations().iterator().next();

        ElementUxf relationUxf = modelUxf.getElements().stream()
                .filter(e -> e.getElementType().equals("Relation"))
                .findFirst()
                .orElse(null);

        assertNotNull(relationUxf);
        assertEquals(relation.getType(), relationUxf.getElementType());
        assertEquals(relation.getUmletAttributes(), relationUxf.getAttributes().getUmletAttributes());
        assertEquals(relation.getMcmModel(), relationUxf.getAttributes().getMcmAttributes().get("model"));
        assertEquals(relation.getPprType(), relationUxf.getAttributes().getMcmAttributes().get("pprType"));
        assertEquals("value1", relationUxf.getAttributes().getMcmAttributes().get("attr1"));
        assertEquals(relation.getTags(), relationUxf.getAttributes().getMcmAttributes().get("tags"));
        assertEquals(relation.getUmletPosition().getX(), relationUxf.getUmletPosition().getX());
        assertEquals(relation.getUmletPosition().getY(), relationUxf.getUmletPosition().getY());
        assertEquals(relation.getUmletPosition().getHeight(), relationUxf.getUmletPosition().getHeight());
        assertEquals(relation.getUmletPosition().getWidth(), relationUxf.getUmletPosition().getWidth());

        ElementUxf srcUxf = modelUxf.getElements().stream()
                .filter(e -> e.getAttributes().getMcmAttributes().get("id").equals(src.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(srcUxf);

        ElementUxf tgtUxf = modelUxf.getElements().stream()
                .filter(e -> e.getAttributes().getMcmAttributes().get("id").equals("tgt"))
                .findFirst()
                .orElse(null);
        assertNotNull(tgtUxf);
    }

    @Test
    public void testToModel_emptyModelUxf() {
        ModelUxf modelUxf = new ModelUxf();
        Model result = mapper.toModel(modelUxf);

        assertNotNull(result);
        assertNull(result.getNodes());
    }

    @Test
    public void testToModel_completeModel() {
        LinkedHashMap<String, Object> mcmAttributes = new LinkedHashMap<>();
        mcmAttributes.put("id", "123");
        mcmAttributes.put("tags", List.of("tag1", "tag2"));
        mcmAttributes.put("attr1", "value1");

        BaseAttributesUxf attributes = new BaseAttributesUxf();
        attributes.setDescription("Description");
        attributes.setOriginalText("Original Text");
        attributes.setMcmAttributes(mcmAttributes);

        ElementUxf nodeUxf = new ElementUxf();
        nodeUxf.setElementType("Node");
        nodeUxf.setAttributes(new ElementAttributesUxf());
        nodeUxf.getAttributes().setMcmAttributes(new LinkedHashMap<>(Map.of("id", "src")));

        ElementUxf relationUxf = new ElementUxf();
        relationUxf.setElementType("Relation");
        relationUxf.setAttributes(new ElementAttributesUxf());
        relationUxf.getAttributes().setMcmAttributes(new LinkedHashMap<>(Map.of("id", "relation")));

        ModelUxf modelUxf = new ModelUxf();
        modelUxf.setZoomLevel(1);
        modelUxf.setAttributes(attributes);
        modelUxf.setElements(Set.of(nodeUxf, relationUxf));

        Model model = mapper.toModel(modelUxf);

        assertEquals(2, model.getNodes().size());
        assertEquals(modelUxf.getAttributes().getMcmAttributes().get("id"), model.getId());
        assertEquals(modelUxf.getAttributes().getDescription(), model.getDescription());
        assertEquals(modelUxf.getAttributes().getMcmAttributes().get("tags"), model.getTags());
        assertEquals(modelUxf.getZoomLevel(), model.getZoomLevel());
        assertEquals(modelUxf.getAttributes().getMcmAttributes().get("attr1"), model.getMcmAttributes().get("attr1"));
    }


    @Test
    public void testPopulateMcmFields() {
        ModelUxf modelUxf = new ModelUxf();
        LinkedHashMap<String, Object> mcmAttributes = new LinkedHashMap<>();
        mcmAttributes.put("key1", "value1");
        mcmAttributes.put("key2", "value2");

        ElementAttributesUxf attributes = new ElementAttributesUxf();
        attributes.setMcmAttributes(mcmAttributes);
        modelUxf.setAttributes(attributes);

        Model model = new Model();
        ModelUxfMapper mapper = new ModelUxfMapperImpl(); // Assume generated MapStruct implementation
        Model result = mapper.populateMcmFields(modelUxf, model);

        assertNotNull(result.getMcmAttributes());
        assertEquals("value1", result.getMcmAttributes().get("key1"));
        assertEquals("value2", result.getMcmAttributes().get("key2"));
    }

    @Test
    public void testPopulateMcmFields_noAttributes() {
        ModelUxf modelUxf = new ModelUxf();
        ElementAttributesUxf attributes = new ElementAttributesUxf();
        modelUxf.setAttributes(attributes);

        Model model = new Model();
        ModelUxfMapper mapper = new ModelUxfMapperImpl(); // Assume generated MapStruct implementation
        Model result = mapper.populateMcmFields(modelUxf, model);

        assertNull(result.getMcmAttributes());
    }

    private static Set<Node> getNodesWithRelations() {
        Node src = new Node();
        src.setElementType("Node");
        src.setTitle("Node Title");
        src.setDescription("Node Description");
        src.setUmletAttributes(new LinkedHashMap<>(Map.of("key", "value")));
        src.setMcmModel("Model");
        src.setOriginalText("Original Text");
        src.setId("src");
        src.setTags(List.of("tag1", "tag2"));

        Node tgt = new Node();
        tgt.setElementType("Node");
        tgt.setId("tgt");

        Relation srcToTgt = getRelation(tgt, "srcToTgt");
        srcToTgt.setId("id");
        Relation tgtToSrc = getRelation(src, "tgtToSrc");
        tgtToSrc.setId("id");

        src.setRelations(Set.of(srcToTgt));
        tgt.setRelations(Set.of(tgtToSrc));

        return Set.of(src, tgt);
    }

    private static Relation getRelation(Node tgt, String id) {
        Relation relation = new Relation();
        relation.setId(id);
        relation.setType("Relation");
        relation.setTitle("Relation Title");
        relation.setDescription("Relation Description");
        relation.setUmletAttributes(new LinkedHashMap<>(Map.of("key", "value")));
        relation.setMcmModel("Model");
        relation.setPprType("Type");
        relation.setOriginalText("Original Text");
        relation.setTags(List.of("tag1", "tag2"));

        relation.setUmletPosition(new UMLetPosition(10, 20, 100, 50));
        relation.setRelativeStartPoint(new RelativePosition(1, 2, 3, 4));
        relation.setRelativeEndPoint(new RelativePosition(5, 6, 7, 8));
        relation.setRelativeMidPoints(List.of(new RelativePosition(9, 10, 11, 12)));
        relation.setMcmAttributes(new LinkedHashMap<>(Map.of("attr1", "value1")));
        relation.setUmletAttributes(new LinkedHashMap<>(Map.of(AttributeKeys.LINE_TYPE, "line")));
        relation.setTarget(tgt);
        return relation;
    }
}
