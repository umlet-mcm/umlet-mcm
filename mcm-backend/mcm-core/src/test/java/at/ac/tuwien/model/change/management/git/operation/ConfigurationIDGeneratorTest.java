package at.ac.tuwien.model.change.management.git.operation;


import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.git.exception.RepositoryWriteException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigurationIDGeneratorTest {

    private final ConfigurationIDGenerator idGenerator = new ConfigurationIDGeneratorImpl();

    @Test
    public void testSetId_model_shouldSetIdOnModel() {
        var model = new Model();
        idGenerator.setID(model);
        Assertions.assertThat(model.getId())
                .isNotNull()
                .hasSize(36);
    }

    @Test
    public void testSetId_node_shouldSetIdOnNode() {
        var node = new Node();
        idGenerator.setID(node);
        Assertions.assertThat(node.getId())
                .isNotNull()
                .hasSize(36);
    }

    @Test
    public void testSetId_relation_shouldSetIdOnRelation() {
        var relation = new Relation();
        idGenerator.setID(relation);
        Assertions.assertThat(relation.getId())
                .isNotNull()
                .hasSize(36);
    }

    @Test
    public void testSetId_idAlreadySet_allowOverwrite_shouldOverwriteIdOnModel() {
        var oldId = "old ID";
        var model = new Model();
        model.setId(oldId);

        idGenerator.setID(model, true);
        Assertions.assertThat(model.getId())
                .isNotNull()
                .hasSize(36)
                .isNotEqualTo(oldId);
    }

    @Test
    public void testSetId_idAlreadySet_disallowOverwrite_shouldThrowRepositoryWriteException() {
        var oldId = "old ID";
        var model = new Model();
        model.setId(oldId);

        Assertions.assertThatThrownBy(() -> idGenerator.setID(model, false))
                .isInstanceOf(RepositoryWriteException.class);
    }
}
