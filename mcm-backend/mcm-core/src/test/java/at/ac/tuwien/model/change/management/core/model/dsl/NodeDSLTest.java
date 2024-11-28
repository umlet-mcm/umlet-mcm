package at.ac.tuwien.model.change.management.core.model.dsl;

import at.ac.tuwien.model.change.management.core.utils.ParsingUtils;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NodeDSL.class)
public class NodeDSLTest {

    @Value("classpath:/dsl/node_dsl_representation.xml")
    private Resource nodeResource;

    @Test
    public void testNodeDSL() throws JAXBException, IOException {
        File xmlFile = nodeResource.getFile();
        assertTrue(xmlFile.exists());

        NodeDSL node = (NodeDSL) ParsingUtils.unmarshalDSL(Files.readString(xmlFile.toPath()));

        assertNotNull(node.getMetadata());
        assertNotNull(node.getMetadata().getCoordinates());
        assertEquals(1, node.getMetadata().getCoordinates().getX());
        assertEquals(2, node.getMetadata().getCoordinates().getY());
        assertEquals(3, node.getMetadata().getCoordinates().getW());
        assertEquals(4, node.getMetadata().getCoordinates().getH());

        assertEquals(1, node.getProperties().size());
        assertEquals("prop1", node.getProperties().getFirst().getKey());
        assertEquals("val1", node.getProperties().getFirst().getValue());

        assertEquals("UMLClass", node.getElementType());
        assertEquals("2", node.getId());
        assertEquals("Test", node.getText());
        assertEquals("PPR", node.getMcmType());

        assertNotNull(node.getTags());
        assertEquals(1, node.getTags().size());
        assertEquals("test", node.getTags().iterator().next());
    }
}
