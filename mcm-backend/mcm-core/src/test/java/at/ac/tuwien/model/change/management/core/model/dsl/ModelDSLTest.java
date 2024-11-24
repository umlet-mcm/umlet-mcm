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

@SpringBootTest(classes = ModelDSL.class)
public class ModelDSLTest {

    @Value("classpath:/dsl/model_dsl_representation.xml")
    private Resource modelResource;


    @Test
    public void testModelDSL() throws JAXBException, IOException {
        File xmlFile = modelResource.getFile();
        assertTrue(xmlFile.exists());

        ModelDSL model = (ModelDSL) ParsingUtils.unmarshalDSL(Files.readString(xmlFile.toPath()));
        assertNotNull(model);

        assertEquals("1", model.getId());
        assertEquals("Model", model.getText());
        assertEquals("PPR", model.getMcmType());
        assertEquals("key1", model.getProperties().getFirst().getKey());
        assertEquals("val1", model.getProperties().getFirst().getValue());
    }
}
