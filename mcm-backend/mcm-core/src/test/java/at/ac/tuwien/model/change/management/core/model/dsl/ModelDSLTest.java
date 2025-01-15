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

@SpringBootTest(classes = {ModelDSL.class, XMLTransformerImpl.class, JaxbConfig.class})
public class ModelDSLTest {

    @Value("classpath:/transformer/model_dsl_representation.xml")
    private Resource modelResource;

    @Autowired
    private XMLTransformerImpl xmlTransformerImpl;


    @Test
    public void testModelDSL() throws JAXBException, IOException {
        File xmlFile = modelResource.getFile();
        assertTrue(xmlFile.exists());

        ModelDSL model = (ModelDSL) xmlTransformerImpl.unmarshal(Files.readString(xmlFile.toPath()));
        assertNotNull(model);

        assertEquals("1", model.getId());
        assertEquals("Model", model.getTitle());
        assertEquals("Model description", model.getDescription());
        assertEquals(2, model.getTags().size());
        assertEquals("key1", model.getProperties().getFirst().getKey());
        assertEquals("val1", model.getProperties().getFirst().getValue());
        assertEquals("ModelModeldescription", model.getMetadata().getOriginalText().replace(" ", "").replace("\n", ""));
    }
}
