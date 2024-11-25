package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.entities.ModelEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;
import java.util.stream.Collectors;

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

        NodeEntity nodeEntity = new NodeEntity();
        when(nodeMapper.toEntity(node)).thenReturn(nodeEntity);

        ModelEntity result = modelEntityMapper.toEntity(model);

        assertEquals("1", result.getId());
        assertEquals(Set.of(nodeEntity), result.getNodes());
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

        Node node = new Node();
        when(nodeMapper.fromEntity(nodeEntity)).thenReturn(node);

        Model result = modelEntityMapper.fromEntity(modelEntity);

        assertEquals("1", result.getId());
        assertEquals(Set.of(node), result.getNodes());
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

        ModelEntity result = modelEntityMapper.toEntity(model);

        assertEquals("1", result.getId());
        assertTrue(result.getNodes().isEmpty());
    }

    @Test
    void fromEntity_withEmptyNodes_returnsModelWithEmptyNodes() {
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setId("1");
        modelEntity.setNodes(Set.of());

        Model result = modelEntityMapper.fromEntity(modelEntity);

        assertEquals("1", result.getId());
        assertTrue(result.getNodes().isEmpty());
    }

    @Test
    void fromEntity_withIdInsideMcmAttributes() {
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setId("1");
        modelEntity.setNodes(Set.of());

        Model result = modelEntityMapper.fromEntity(modelEntity);

        assertEquals("1", result.getId());
        assertNotNull(result.getMcmAttributes());
        assertFalse(result.getMcmAttributes().isEmpty());
        assertEquals("1", result.getMcmAttributes().get("Id"));
        assertEquals(1, result.getMcmAttributes().size());
        assertTrue(result.getNodes().isEmpty());
    }
}