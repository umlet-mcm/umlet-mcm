package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RelationTest {

    @Test
    void testFromNode() {
        Node n = new Node();
        n.setId("ID");

        n.setElementType("UMLClass");
        assertNull(Relation.fromNode(n, 10));

        n.setElementType("Relation");
        n.setUmletPosition(new UMLetPosition(10, 10, 50, 50));
        n.setUmletAttributes(new LinkedHashMap<>());
        n.getUmletAttributes().put(AttributeKeys.LINE_TYPE, "->>");

        n.setGeneratedAttributes(List.of(1, 1, 1)); // not enough values
        assertNull(Relation.fromNode(n, 10));

        n.setGeneratedAttributes(List.of(10, 10, 20, 20, 30, 30, 40, 40));
        Relation rel = Relation.fromNode(n, 10);
        assertNotNull(rel);
        assertEquals(List.of(
                new RelativePosition(20, 20, 10, 10),
                new RelativePosition(30, 30, 10, 10)
        ), rel.getRelativeMidPoints());

        assertEquals(new Point(50, 50), rel.getEndPoint());
    }
}
