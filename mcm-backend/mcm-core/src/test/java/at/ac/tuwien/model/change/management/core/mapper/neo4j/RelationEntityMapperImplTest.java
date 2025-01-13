package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
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

class RelationEntityMapperImplTest {

    @Mock
    private NodeEntityMapper nodeMapper;

    @InjectMocks
    private RelationEntityMapperImpl relationEntityMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toEntity_withValidRelation_returnsCorrectRelationEntity() {
        Relation relation = new Relation();
        relation.setId("1");
        relation.setTitle("Affected by");
        relation.setDescription("Relation description");
        relation.setType("CEN");
        relation.setMcmAttributes(new LinkedHashMap<>(Map.of("property1", "key1")));
        relation.setUmletAttributes(new LinkedHashMap<>(Map.of("dashed", "true")));
        relation.setTags(List.of("tag1", "tag2"));
        Node targetNode = new Node();
        relation.setTarget(targetNode);

        NodeEntity targetNodeEntity = new NodeEntity();
        when(nodeMapper.toEntity(targetNode)).thenReturn(targetNodeEntity);

        RelationEntity result = relationEntityMapper.toEntity(relation);

        assertEquals("1", result.getId());
        assertEquals("Affected by", result.getName());
        assertEquals("Relation description", result.getDescription());
        assertEquals("CEN", result.getType());
        assertEquals(Map.of("property1", new StringValue("key1")), result.getProperties());
        assertEquals(Map.of("dashed", "true"), result.getUmletProperties());
        assertEquals(Set.of("tag1", "tag2"), result.getTags());
        assertEquals(targetNodeEntity, result.getTarget());
    }

    @Test
    void toEntity_withNullRelation_returnsNull() {
        RelationEntity result = relationEntityMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void fromEntity_withValidRelationEntity_returnsCorrectRelation() {
        RelationEntity relationEntity = new RelationEntity();
        relationEntity.setId("1");
        relationEntity.setName("Causes");
        relationEntity.setDescription("Relation description");
        relationEntity.setType("PAN");
        relationEntity.setProperties(Map.of("property1", new StringValue("key1")));
        relationEntity.setUmletProperties(Map.of("dashed", "false"));
        relationEntity.setTags(Set.of("tag1", "tag2"));
        NodeEntity targetNodeEntity = new NodeEntity();
        relationEntity.setTarget(targetNodeEntity);

        Node targetNode = new Node();
        when(nodeMapper.fromEntity(targetNodeEntity)).thenReturn(targetNode);

        Relation result = relationEntityMapper.fromEntity(relationEntity);

        assertEquals("1", result.getId());
        assertEquals("Causes", result.getTitle());
        assertEquals("Relation description", result.getDescription());
        assertEquals("PAN", result.getType());
        assertEquals(Map.of("property1", "key1"), result.getMcmAttributes());
        assertEquals(Map.of("dashed", "false"), result.getUmletAttributes());
        assertTrue(result.getTags().contains("tag1"));
        assertTrue(result.getTags().contains("tag2"));
        assertEquals(targetNode, result.getTarget());
    }

    @Test
    void fromEntity_withNullRelationEntity_returnsNull() {
        Relation result = relationEntityMapper.fromEntity(null);

        assertNull(result);
    }
}