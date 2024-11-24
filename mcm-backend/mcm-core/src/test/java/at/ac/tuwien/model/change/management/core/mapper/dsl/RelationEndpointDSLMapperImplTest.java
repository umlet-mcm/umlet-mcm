package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationEndpointDSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        RelationEndpointDSLMapperImpl.class
})
class RelationEndpointDSLMapperImplTest {

    @Autowired
    private RelationEndpointDSLMapperImpl mapper;

    @Test
    void testToDSL() {
        Node node = new Node();
        node.setId("nodeId");
        node.setDescription("description");

        RelationEndpointDSL result = mapper.toDSL(node);

        assertNotNull(result);
        assertEquals("nodeId", result.getId());
        assertEquals("description", result.getText());
    }

    @Test
    void testToDSL_NullInput() {
        RelationEndpointDSL result = mapper.toDSL(null);
        assertNull(result);
    }

    @Test
    void testToDSL_NodeWithNullAttributes() {
        Node node = new Node();
        node.setId(null);
        node.setDescription(null);

        RelationEndpointDSL result = mapper.toDSL(node);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getText());
    }
}
