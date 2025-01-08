package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.*;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RelationUpdaterImplTest {

    @InjectMocks
    private RelationUpdaterImpl relationUpdater;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateRelation_updatesAllFields() {
        Relation relation = new Relation();
        relation.setId("1");
        relation.setTitle("New Title");
        relation.setDescription("New Description");
        relation.setType("New Type");
        val mcmAttributes = new LinkedHashMap<String, Object>();
        mcmAttributes.put("key1", "attr2");
        relation.setMcmAttributes(mcmAttributes);
        val umletAttributes = new LinkedHashMap<String, String>();
        umletAttributes.put("UMLkey1", "UMLattr2");
        relation.setUmletAttributes(umletAttributes);
        relation.setTags(List.of("tag1", "tag2"));
        val target = new Node();
        target.setId("Node1");
        relation.setTarget(target);

        // Not updated properties
        relation.setUmletPosition(null);
        relation.setEndPoint(null);
        relation.setStartPoint(null);
        relation.setMcmModel("");
        relation.setMcmModelId("");
        relation.setPprType("");


        Relation relationToUpdate = new Relation();
        relationToUpdate.setId("2");
        relationToUpdate.setTitle("Old Title");
        relationToUpdate.setDescription("Old Description");
        relationToUpdate.setType("Old Type");
        val mcmAttributes1 = new LinkedHashMap<String, Object>();
        mcmAttributes1.put("key5", "attr5");
        relationToUpdate.setMcmAttributes(mcmAttributes1);
        val umletAttributes1 = new LinkedHashMap<String, String>();
        umletAttributes1.put("UMLkey5", "UMLattr5");
        relationToUpdate.setUmletAttributes(umletAttributes1);
        relationToUpdate.setTags(List.of("tag3"));
        val target1 = new Node();
        target1.setId("Node2");
        relationToUpdate.setTarget(target1);

        // Not updated properties
        relationToUpdate.setUmletPosition(new UMLetPosition(5, 6, 7, 8));
        relationToUpdate.setEndPoint(new Point(1, 2));
        relationToUpdate.setStartPoint(new Point(3, 4));
        relationToUpdate.setRelativeEndPoint(new RelativePosition(1, 2,3,4));
        relationToUpdate.setRelativeMidPoints(List.of(new RelativePosition(1, 2,3,4)));
        relationToUpdate.setRelativeStartPoint(new RelativePosition(1, 2,3,4));
        relationToUpdate.setMcmModel("PAN");
        relationToUpdate.setMcmModelId("ID5");
        relationToUpdate.setPprType("PPR_TYPE");

        relationUpdater.updateRelation(relation, relationToUpdate);

        assertEquals("1", relationToUpdate.getId());
        assertEquals("New Title", relationToUpdate.getTitle());
        assertEquals("New Description", relationToUpdate.getDescription());
        assertEquals("New Type", relationToUpdate.getType());
        assertEquals(mcmAttributes, relationToUpdate.getMcmAttributes());
        assertEquals(umletAttributes, relationToUpdate.getUmletAttributes());
        assertEquals(List.of("tag1", "tag2"), relationToUpdate.getTags());
        assertEquals(target, relationToUpdate.getTarget());
        assertEquals(new UMLetPosition(5, 6, 7, 8), relationToUpdate.getUmletPosition());
        assertEquals(new Point(4, 6), relationToUpdate.getEndPoint());
        assertEquals(new Point(4, 6), relationToUpdate.getStartPoint());
        assertEquals(new RelativePosition(1, 2,3,4), relationToUpdate.getRelativeEndPoint());
        assertEquals(List.of(new RelativePosition(1, 2,3,4)), relationToUpdate.getRelativeMidPoints());
        assertEquals(new RelativePosition(1, 2,3,4), relationToUpdate.getRelativeStartPoint());
        assertEquals("PAN", relationToUpdate.getMcmModel());
        assertEquals("ID5", relationToUpdate.getMcmModelId());
        assertEquals("PPR_TYPE", relationToUpdate.getPprType());
    }

    @Test
    void updateRelation_nullRelation_doesNothing() {
        Relation relationToUpdate = new Relation();
        relationToUpdate.setTitle("Old Title");

        relationUpdater.updateRelation(null, relationToUpdate);

        assertEquals("Old Title", relationToUpdate.getTitle());
    }

    @Test
    void updateRelation_nullRelationToUpdate_doesNothing() {
        Relation relation = new Relation();
        relation.setTitle("New Title");

        relationUpdater.updateRelation(relation, null);

        // No assertions needed as we are verifying no exceptions are thrown
    }

    @Test
    void updateRelation_bothRelationsNull_doesNothing() {
        relationUpdater.updateRelation(null, null);

        // No assertions needed as we are verifying no exceptions are thrown
    }
}