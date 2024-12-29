package at.ac.tuwien.model.change.management.core.transformer;


import at.ac.tuwien.model.change.management.core.model.dsl.*;
import at.ac.tuwien.model.change.management.core.model.intermediary.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class XMLTransformerTest {

    private XMLTransformer transformer;

    @BeforeEach
    void setUp() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(
                ModelDSL.class,
                NodeDSL.class,
                RelationDSL.class,
                ConfigurationUxf.class,
                ModelUxf.class
        );
        transformer = new XMLTransformerImpl(jaxbContext);
    }

    @Test
    void testMarshalAndUnmarshalModelDSL() throws JAXBException {
        ModelDSL modelDSL = new ModelDSL();
        modelDSL.setId("model-123");
        modelDSL.setTitle("Test Model");
        modelDSL.setDescription("A test description");
        modelDSL.setTags(Collections.singletonList("testTag"));

        PropertyDSL property = new PropertyDSL();
        property.setKey("propKey");
        property.setValue("propValue");
        modelDSL.setProperties(Collections.singletonList(property));

        MetadataDSL metadata = new MetadataDSL();
        modelDSL.setMetadata(metadata);
        modelDSL.setZoomLevel(5);

        String xml = transformer.marshal(modelDSL);
        assertNotNull(xml);
        assertTrue(xml.contains("Test Model"));
        assertTrue(xml.contains("testTag"));
        assertTrue(xml.contains("propKey"));

        Object unmarshalled = transformer.unmarshal(xml);
        assertInstanceOf(ModelDSL.class, unmarshalled);

        ModelDSL unmarshalledModel = (ModelDSL) unmarshalled;
        assertEquals(modelDSL.getId(), unmarshalledModel.getId());
        assertEquals(modelDSL.getTitle(), unmarshalledModel.getTitle());
        assertEquals(modelDSL.getDescription(), unmarshalledModel.getDescription());
        assertEquals(modelDSL.getTags().size(), unmarshalledModel.getTags().size());
        assertEquals(modelDSL.getTags().get(0), unmarshalledModel.getTags().get(0));
        assertEquals(modelDSL.getProperties().size(), unmarshalledModel.getProperties().size());
    }

    @Test
    void testUnmarshalFromInputStream() throws JAXBException {
        String xml = """
                <model>
                    <id>model-123</id>
                    <title>Stream Title</title>
                    <description>Stream Desc</description>
                    <zoom_level>10</zoom_level>
                </model>
                """;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        Object unmarshalled = transformer.unmarshal(inputStream);
        assertInstanceOf(ModelDSL.class, unmarshalled);
        ModelDSL model = (ModelDSL) unmarshalled;
        assertEquals("model-123", model.getId());
        assertEquals("Stream Title", model.getTitle());
        assertEquals("Stream Desc", model.getDescription());
        assertEquals(10, model.getZoomLevel());
    }

    @Test
    void testMarshalUxfConfiguration() throws JAXBException {
        ConfigurationUxf configUxf = new ConfigurationUxf();
        configUxf.setZoomLevel(100);

        String uxfXml = transformer.marshalUxf(configUxf);
        assertNotNull(uxfXml);
        assertTrue(uxfXml.contains("<diagram>"));
        assertTrue(uxfXml.contains("<zoom_level>100</zoom_level>"));

        Object unmarshalled = transformer.unmarshal(uxfXml);
        assertTrue(unmarshalled instanceof ModelUxf);
        ModelUxf modelUxf = (ModelUxf) unmarshalled;
        assertEquals(100, modelUxf.getZoomLevel());
    }

    @Test
    void testMarshalUxfModel() throws JAXBException {
        ModelUxf modelUxf = new ModelUxf();
        modelUxf.setZoomLevel(100);

        ElementUxf elementUxf = new ElementUxf();
        elementUxf.setElementType("UMLClass");
        elementUxf.setAttributes(new ElementAttributesUxf());
        modelUxf.setElements(Set.of(elementUxf));
        modelUxf.setAttributes(new BaseAttributesUxf());

        String uxfXml = transformer.marshalUxf(modelUxf);
        assertNotNull(uxfXml);
        assertTrue(uxfXml.contains("<diagram>"));
        assertTrue(uxfXml.contains("<zoom_level>100</zoom_level>"));

        Object unmarshalled = transformer.unmarshal(uxfXml);
        assertTrue(unmarshalled instanceof ModelUxf);
        ModelUxf modelUxfUnmarshalled = (ModelUxf) unmarshalled;
        assertEquals(100, modelUxfUnmarshalled.getZoomLevel());
    }
}

