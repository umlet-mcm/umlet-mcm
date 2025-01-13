package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NodeUpdaterImplTest {

    @Mock
    private RelationUpdater relationUpdater;

    @InjectMocks
    private NodeUpdaterImpl nodeUpdater;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateNode_updatesAllFields() {
        Node node = new Node();
        node.setId("1");
        node.setTitle("New Title");
        node.setDescription("New Description");
        node.setElementType("New Type");
        node.setPprType("New PPR Type");
        val mcmAttributes = new LinkedHashMap<String, Object>();
        mcmAttributes.put("key1", "attr2");
        node.setMcmAttributes(mcmAttributes);
        val umletAttributes = new LinkedHashMap<String, String>();
        mcmAttributes.put("UMLkey1", "UMLattr2");
        node.setUmletAttributes(umletAttributes);
        node.setTags(List.of("tag1", "tag2"));
        node.setUmletPosition(new UMLetPosition(1, 2, 3, 4));

        // Not updated properties
        node.setGeneratedAttributes(List.of(1,2));
        node.setMcmModelId("");
        node.setMcmModel("");


        Relation relation1 = new Relation();
        relation1.setId("rel1");
        node.setRelations(Set.of(relation1));

        Node nodeToUpdate = new Node();
        nodeToUpdate.setId("1");
        nodeToUpdate.setTitle("Old Title");
        nodeToUpdate.setDescription("Old Description");
        nodeToUpdate.setElementType("Old Type");
        nodeToUpdate.setPprType("Old PPR Type");
        val mcmAttributes1 = new LinkedHashMap<String, Object>();
        mcmAttributes.put("key5", "attr5");
        nodeToUpdate.setMcmAttributes(mcmAttributes1);
        val umletAttributes1 = new LinkedHashMap<String, String>();
        mcmAttributes.put("UMLkey5", "UMLattr5");
        nodeToUpdate.setUmletAttributes(umletAttributes1);
        nodeToUpdate.setTags(List.of("tag3"));
        nodeToUpdate.setUmletPosition(new UMLetPosition(5, 6, 7, 8));

        // Not updated properties
        nodeToUpdate.setGeneratedAttributes(List.of(3,4));
        nodeToUpdate.setMcmModelId("mcmModelId");
        nodeToUpdate.setMcmModel("PAN");

        Relation relationToUpdate1 = new Relation();
        relationToUpdate1.setId("rel1");
        nodeToUpdate.setRelations(Set.of(relationToUpdate1));

        nodeUpdater.updateNode(node, nodeToUpdate);

        assertEquals("New Title", nodeToUpdate.getTitle());
        assertEquals("New Description", nodeToUpdate.getDescription());
        assertEquals("New Type", nodeToUpdate.getElementType());
        assertEquals("New PPR Type", nodeToUpdate.getPprType());
        assertEquals(mcmAttributes, nodeToUpdate.getMcmAttributes());
        assertEquals(umletAttributes, nodeToUpdate.getUmletAttributes());
        assertEquals(List.of("tag1", "tag2"), nodeToUpdate.getTags());
        assertEquals(new UMLetPosition(1,2,3,4), nodeToUpdate.getUmletPosition());
        assertEquals(List.of(3,4), nodeToUpdate.getGeneratedAttributes());
        assertEquals("mcmModelId", nodeToUpdate.getMcmModelId());
        assertEquals("PAN", nodeToUpdate.getMcmModel());
        verify(relationUpdater, times(1)).updateRelation(eq(relation1), any());
    }

    @Test
    void updateNode_nullNode_doesNothing() {
        Node nodeToUpdate = new Node();
        nodeToUpdate.setTitle("Old Title");

        nodeUpdater.updateNode(null, nodeToUpdate);

        assertEquals("Old Title", nodeToUpdate.getTitle());
        verifyNoInteractions(relationUpdater);
    }

    @Test
    void updateNode_nullNodeToUpdate_doesNothing() {
        Node node = new Node();
        node.setTitle("New Title");

        nodeUpdater.updateNode(node, null);

        verifyNoInteractions(relationUpdater);
    }

    @Test
    void updateNode_noMatchingRelations_doesNotUpdateRelations() {
        Node node = new Node();
        node.setId("1");
        Relation relation1 = new Relation();
        relation1.setId("rel1");
        node.setRelations(Set.of(relation1));

        Node nodeToUpdate = new Node();
        nodeToUpdate.setId("1");
        Relation relationToUpdate1 = new Relation();
        relationToUpdate1.setId("rel2");
        nodeToUpdate.setRelations(Set.of(relationToUpdate1));

        nodeUpdater.updateNode(node, nodeToUpdate);

        verify(relationUpdater, only()).updateRelation(eq(null), any());
    }
}