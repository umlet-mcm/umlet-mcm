package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NodeEntityMapperImplTest {

    @Mock
    private PositionMapper positionMapper;

    @Mock
    private RelationEntityMapper relationEntityMapper;

    @Mock
    private CycleAvoidingMappingContext context;

    @InjectMocks
    private NodeEntityMapperImpl nodeEntityMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toEntityNode_withValidNode_returnsCorrectNodeEntity() {
        Node node = new Node();
        node.setId(UUID.randomUUID().toString());
        node.setTitle("Do A Photo");
        node.setDescription("void doAPhoto() {}; String name = \"photo\";");
        node.setElementType("Process");
        node.setPprType("Process");
        node.setUmletAttributes(new LinkedHashMap<>(Map.of("background", "red")));
        node.setMcmAttributes(new LinkedHashMap<>(Map.of("camera", "Sony")));
        node.setTags(List.of("NEW", "IN-PROCESS"));
        UMLetPosition position = new UMLetPosition();
        node.setUmletPosition(position);
        Relation relation = new Relation();
        node.setRelations(Set.of(relation));

        RelationEntity relationEntity = new RelationEntity();

        when(context.getMappedInstance(node, NodeEntity.class)).thenReturn(null);
        when(relationEntityMapper.toEntity(relation)).thenReturn(relationEntity);
        when(positionMapper.toGraphProperties(position)).thenReturn(Map.of("x", 10, "y", 20, "width", 30, "height", 40));

        NodeEntity result = nodeEntityMapper.toEntity(node);

        assertEquals(node.getId(), result.getGeneratedID());
        assertEquals("Do A Photo", result.getName());
        assertEquals("void doAPhoto() {}; String name = \"photo\";", result.getDescription());
        assertEquals("Process", result.getType());
        assertEquals("Process", result.getPprType());
        assertEquals(Map.of("background", "red"), result.getUmletProperties());
        assertEquals(Map.of("camera", "Sony"), result.getProperties());
        assertEquals(Set.of("NEW", "IN-PROCESS"), result.getTags());
        assertEquals(Map.of("x", 10, "y", 20, "width", 30, "height", 40), result.getPosition());
        assertEquals(Set.of(relationEntity), result.getRelations());

    }

    @Test
    void toEntityNode_withNullNode_returnsNull() {
        NodeEntity result = nodeEntityMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void fromEntityNode_withValidNodeEntity_returnsCorrectNode() {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setGeneratedID(UUID.randomUUID().toString());
        nodeEntity.setName("Do A Photo");
        nodeEntity.setDescription("void doAPhoto() {}; String name = \"photo\";");
        nodeEntity.setType("Process");
        nodeEntity.setPprType("Process");
        nodeEntity.setProperties(Map.of("camera", "Sony"));
        nodeEntity.setUmletProperties(Map.of("background", "red"));
        nodeEntity.setTags(Set.of("NEW", "IN-PROCESS"));
        nodeEntity.setPosition(Map.of("x", 10, "y", 20, "width", 30, "height", 40));
        RelationEntity relationEntity = new RelationEntity();
        nodeEntity.setRelations(Set.of(relationEntity));


        UMLetPosition position = new UMLetPosition();
        Relation relation = new Relation();

        when(context.getMappedInstance(nodeEntity, Node.class)).thenReturn(null);
        when(relationEntityMapper.fromEntity(relationEntity)).thenReturn(relation);
        when(positionMapper.toLocation(nodeEntity.getPosition())).thenReturn(position);

        Node result = nodeEntityMapper.fromEntity(nodeEntity);


        assertEquals(nodeEntity.getGeneratedID(), result.getId());
        assertEquals("Do A Photo", result.getTitle());
        assertEquals("void doAPhoto() {}; String name = \"photo\";", result.getDescription());
        assertEquals("Process", result.getElementType());
        assertEquals("Process", result.getPprType());
        assertEquals(Map.of("camera", "Sony"), result.getMcmAttributes());
        assertEquals(Map.of("background", "red"), result.getUmletAttributes());
        assertTrue(result.getTags().contains("NEW"));
        assertTrue(result.getTags().contains("IN-PROCESS"));
        assertEquals(position, result.getUmletPosition());
        assertEquals(Set.of(relation), result.getRelations());
    }

    @Test
    void fromEntityNode_withNullNodeEntity_returnsNull() {
        Node result = nodeEntityMapper.fromEntity(null);

        assertNull(result);
    }

    @Test
    void toEntityNode_alreadyMapped_returnsMapped() {
        Node node = new Node();
        NodeEntity nodeEntity = new NodeEntity();

        when(context.getMappedInstance(node, NodeEntity.class)).thenReturn(nodeEntity);

        NodeEntity result = nodeEntityMapper.toEntity(node);

        assertEquals(nodeEntity, result);
    }

    @Test
    void fromEntityNode_alreadyMapped_returnsMapped() {
        NodeEntity nodeEntity = new NodeEntity();
        Node node = new Node();

        when(context.getMappedInstance(nodeEntity, Node.class)).thenReturn(node);

        Node result = nodeEntityMapper.fromEntity(nodeEntity);

        assertEquals(node, result);
    }
}