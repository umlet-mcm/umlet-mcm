package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.entities.ModelEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neo4j.driver.internal.value.StringValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelEntityMapperImplTest {

    @Mock
    private NodeEntityMapper nodeMapper;

    @InjectMocks
    private ModelEntityMapperImpl modelEntityMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toEntity_withValidModel_returnsCorrectModelEntity() {
        Model model = new Model();
        model.setId("1");
        Node node = new Node();
        model.setNodes(Set.of(node));
        model.setTags(List.of("tag1", "tag2"));
        model.setMcmAttributes(new LinkedHashMap<>(Map.of("property1", "key1", "property2", "key2")));
        model.setTitle("Model Name");
        model.setDescription("Model Description");

        NodeEntity nodeEntity = new NodeEntity();
        when(nodeMapper.toEntity(node)).thenReturn(nodeEntity);

        ModelEntity result = modelEntityMapper.toEntity(model);

        assertEquals("1", result.getId());
        assertEquals(Set.of(nodeEntity), result.getNodes());
        assertEquals(Set.of("tag1", "tag2"), result.getTags());
        assertEquals(Map.of("property1", new StringValue("key1"), "property2", new StringValue("key2")), result.getProperties());
        assertEquals("Model Name", result.getName());
        assertEquals("Model Description", result.getDescription());
    }

    @Test
    void toEntity_withNullModel_returnsNull() {
        ModelEntity result = modelEntityMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void fromEntity_withValidModelEntity_returnsCorrectModel() {
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setId("1");
        NodeEntity nodeEntity = new NodeEntity();
        modelEntity.setNodes(Set.of(nodeEntity));
        modelEntity.setTags(Set.of("tag1", "tag2"));
        modelEntity.setProperties(Map.of("property1", new StringValue("key1"), "property2", new StringValue("key2")));
        modelEntity.setName("Model Name");
        modelEntity.setDescription("Model Description");

        Node node = new Node();
        when(nodeMapper.fromEntity(nodeEntity)).thenReturn(node);

        Model result = modelEntityMapper.fromEntity(modelEntity);

        assertEquals("1", result.getId());
        assertEquals(Set.of(node), result.getNodes());
        assertTrue(result.getTags().contains("tag1"));
        assertTrue(result.getTags().contains("tag2"));
        assertEquals(Map.of("property1", "key1", "property2", "key2"), result.getMcmAttributes());
        assertEquals("Model Name", result.getTitle());
        assertEquals("Model Description", result.getDescription());
    }

    @Test
    void fromEntity_withNullModelEntity_returnsNull() {
        Model result = modelEntityMapper.fromEntity(null);

        assertNull(result);
    }

    @Test
    void toEntity_withEmptyNodes_returnsModelEntityWithEmptyNodes() {
        Model model = new Model();
        model.setId("1");
        model.setNodes(Set.of());
        model.setTags(List.of("tag1", "tag2"));
        model.setMcmAttributes(new LinkedHashMap<>(Map.of("property1", "key1", "property2", "key2")));
        model.setTitle("Model Name");
        model.setDescription("Model Description");


        ModelEntity result = modelEntityMapper.toEntity(model);

        assertEquals("1", result.getId());
        assertTrue(result.getNodes().isEmpty());
        assertEquals(Set.of("tag1", "tag2"), result.getTags());
        assertEquals(Map.of("property1", new StringValue("key1"), "property2", new StringValue("key2")), result.getProperties());
        assertEquals("Model Name", result.getName());
        assertEquals("Model Description", result.getDescription());

    }

    @Test
    void fromEntity_withEmptyNodes_returnsModelWithEmptyNodes() {
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setId("1");
        modelEntity.setNodes(Set.of());
        modelEntity.setTags(Set.of("tag1", "tag2"));
        modelEntity.setProperties(Map.of("property1", new StringValue("key1"), "property2", new StringValue("key2")));
        modelEntity.setName("Model Name");
        modelEntity.setDescription("Model Description");


        Model result = modelEntityMapper.fromEntity(modelEntity);

        assertEquals("1", result.getId());
        assertTrue(result.getNodes().isEmpty());
        assertTrue(result.getTags().contains("tag1"));
        assertTrue(result.getTags().contains("tag2"));
        assertEquals(Map.of("property1", "key1", "property2", "key2"), result.getMcmAttributes());
        assertEquals("Model Name", result.getTitle());
        assertEquals("Model Description", result.getDescription());
    }
}