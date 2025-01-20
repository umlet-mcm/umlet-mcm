package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ModelUpdaterImplTest {

    @Mock
    private NodeUpdaterImpl nodeUpdater;

    @InjectMocks
    private ModelUpdaterImpl modelUpdater;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateModel_updatesAllFields() {
        Model model = new Model();
        model.setId("1");
        model.setTitle("New Title");
        model.setDescription("New Description");
        model.setTags(List.of("tag1", "tag2"));
        val mcmAttributes = new LinkedHashMap<String, Object>();
        mcmAttributes.put("key1", "attr2");
        model.setMcmAttributes(mcmAttributes);

        Node node1 = new Node();
        node1.setId("node1");
        model.setNodes(Set.of(node1));

        Model modelToUpdate = new Model();
        modelToUpdate.setId("1");
        modelToUpdate.setTitle("Old Title");
        modelToUpdate.setDescription("Old Description");
        modelToUpdate.setTags(List.of("tag3"));
        val mcmAttributes1 = new LinkedHashMap<String, Object>();
        mcmAttributes1.put("key1", "attr3");
        modelToUpdate.setMcmAttributes(mcmAttributes1);

        Node nodeToUpdate1 = new Node();
        nodeToUpdate1.setId("node1");
        modelToUpdate.setNodes(Set.of(nodeToUpdate1));

        modelUpdater.updateModel(model, modelToUpdate);

        assertEquals("New Title", modelToUpdate.getTitle());
        assertEquals("New Description", modelToUpdate.getDescription());
        assertEquals(List.of("tag1", "tag2"), modelToUpdate.getTags());
        assertEquals(mcmAttributes, modelToUpdate.getMcmAttributes());
        assertEquals(10, modelToUpdate.getZoomLevel());
        verify(nodeUpdater, times(1)).updateNode(eq(node1), any());
    }

    @Test
    void updateModel_nullModel_doesNothing() {
        Model modelToUpdate = new Model();
        modelToUpdate.setTitle("Old Title");

        modelUpdater.updateModel(null, modelToUpdate);

        assertEquals("Old Title", modelToUpdate.getTitle());
        verifyNoInteractions(nodeUpdater);
    }

    @Test
    void updateModel_nullModelToUpdate_doesNothing() {
        Model model = new Model();
        model.setTitle("New Title");

        modelUpdater.updateModel(model, null);

        verifyNoInteractions(nodeUpdater);
    }

    @Test
    void updateModel_noMatchingNodes_doesNotUpdateNodes() {
        Model model = new Model();
        model.setId("1");
        Node node1 = new Node();
        node1.setId("node1");
        model.setNodes(Set.of(node1));

        Model modelToUpdate = new Model();
        modelToUpdate.setId("1");
        Node nodeToUpdate1 = new Node();
        nodeToUpdate1.setId("node2");
        modelToUpdate.setNodes(Set.of(nodeToUpdate1));

        modelUpdater.updateModel(model, modelToUpdate);

        verify(nodeUpdater, only()).updateNode(eq(null), any());
    }
}