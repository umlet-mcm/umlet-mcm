package at.ac.tuwien.model.change.management.core.model.dsl;

import at.ac.tuwien.model.change.management.core.configuration.JaxbConfig;
import at.ac.tuwien.model.change.management.core.transformer.XMLTransformerImpl;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {RelationDSL.class, XMLTransformerImpl.class, JaxbConfig.class})
public class RelationDSLTest {

    @Value("classpath:/transformer/relation_dsl_representation.xml")
    private Resource relationResource;

    @Autowired
    private XMLTransformerImpl xmlTransformerImpl;

    @Test
    public void testRelationDSL() throws JAXBException, IOException {
        File xmlFile = relationResource.getFile();
        assertTrue(xmlFile.exists());

        RelationDSL relation = (RelationDSL) xmlTransformerImpl.unmarshal(Files.readString(xmlFile.toPath()));
        assertNotNull(relation);

        assertNotNull(relation.getMetadata());
        assertNotNull(relation.getMetadata().getCoordinates());
        assertEquals(1, relation.getMetadata().getCoordinates().getX());
        assertEquals(2, relation.getMetadata().getCoordinates().getY());
        assertEquals(3, relation.getMetadata().getCoordinates().getW());
        assertEquals(4, relation.getMetadata().getCoordinates().getH());

        assertEquals(1, relation.getProperties().size());
        assertEquals("key1", relation.getProperties().getFirst().getKey());
        assertEquals("val1", relation.getProperties().getFirst().getValue());

        assertEquals("1", relation.getId());
        assertEquals("Relation", relation.getTitle());

        assertNotNull(relation.getSource());
        assertEquals("2", relation.getSource().getId());
        assertEquals("source", relation.getSource().getText());

        assertNotNull(relation.getTarget());
        assertEquals("3", relation.getTarget().getId());
        assertEquals("target", relation.getTarget().getText());

        assertEquals(1, relation.getMetadata().getPositions().getRelativeStartPoint().getAbsX());
        assertEquals(2, relation.getMetadata().getPositions().getRelativeStartPoint().getAbsY());
        assertEquals(3, relation.getMetadata().getPositions().getRelativeStartPoint().getOffsetX());
        assertEquals(4, relation.getMetadata().getPositions().getRelativeStartPoint().getOffsetY());

        assertEquals(1, relation.getMetadata().getPositions().getRelativeEndPoint().getAbsX());
        assertEquals(2, relation.getMetadata().getPositions().getRelativeEndPoint().getAbsY());
        assertEquals(3, relation.getMetadata().getPositions().getRelativeEndPoint().getOffsetX());
        assertEquals(4, relation.getMetadata().getPositions().getRelativeEndPoint().getOffsetY());
    }
}
