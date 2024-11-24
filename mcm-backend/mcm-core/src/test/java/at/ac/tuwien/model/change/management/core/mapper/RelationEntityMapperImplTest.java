package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        relation.setType("CEN");
        Node targetNode = new Node();
        relation.setTarget(targetNode);

        NodeEntity targetNodeEntity = new NodeEntity();
        when(nodeMapper.toEntity(targetNode)).thenReturn(targetNodeEntity);

        RelationEntity result = relationEntityMapper.toEntity(relation);

        assertEquals("CEN", result.getType());
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
        relationEntity.setType("PAN");
        NodeEntity targetNodeEntity = new NodeEntity();
        relationEntity.setTarget(targetNodeEntity);

        Node targetNode = new Node();
        when(nodeMapper.fromEntity(targetNodeEntity)).thenReturn(targetNode);

        Relation result = relationEntityMapper.fromEntity(relationEntity);

        assertEquals("PAN", result.getType());
        assertEquals(targetNode, result.getTarget());
    }

    @Test
    void fromEntity_withNullRelationEntity_returnsNull() {
        Relation result = relationEntityMapper.fromEntity(null);

        assertNull(result);
    }
}