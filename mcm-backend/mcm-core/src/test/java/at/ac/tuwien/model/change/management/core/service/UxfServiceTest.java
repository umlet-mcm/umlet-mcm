package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.server.ModelChangeManagementServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ModelChangeManagementServer.class})
public class UxfServiceTest {
    @Autowired
    private UxfServiceImpl service;

    @Test
    void testCreateConfigurationFromUxf() throws UxfException {
        String in = "uxf/frag1_cost_updated.uxf";
        assertDoesNotThrow(() -> service.createConfigurationFromUxf(getStream(in)));
        Configuration res = service.createConfigurationFromUxf(getStream(in));
        assertEquals(1, res.getModels().size());
        assertEquals(7, res.getModels().stream()
                .findFirst()
                .orElseThrow()
                .getNodes().size());
    }

    @Test
    void testAddUxfToConfiguration() throws UxfException {
        String in1 = "uxf/frag1_cost_updated.uxf";
        Configuration target = service.createConfigurationFromUxf(getStream(in1));

        String in2 = "uxf/frag2.uxf";
        Configuration res = service.addUxfToConfiguration(getStream(in2), target.getName());

        assertEquals(2, res.getModels().size());

        assertThrows(ConfigurationException.class, () -> service.addUxfToConfiguration(getStream(in2), "invalid_uuid"));
    }

    @Test
    void testExportModel() throws UxfException, ModelNotFoundException {
        String in = "uxf/frag1_cost_updated.uxf";
        Configuration conf = service.createConfigurationFromUxf(getStream(in));
        String targetId = conf.getModels().stream().findFirst().orElseThrow().getId();

        assertDoesNotThrow(()->service.exportModel(targetId));
        String res = service.exportModel(targetId);
        assertNotNull(res);
        assertTrue(res.startsWith("<diagram>"));

        assertThrows(ModelNotFoundException.class, ()->service.exportModel("invalid_uuid"));
    }

    @Test
    void testExportConfiguration() throws UxfException, ConfigurationException {
        String in = "uxf/frag1_cost_updated.uxf";
        Configuration conf = service.createConfigurationFromUxf(getStream(in));

        assertDoesNotThrow(()->service.exportConfiguration(conf.getName()));
        String res = service.exportConfiguration(conf.getName());
        assertNotNull(res);
        assertTrue(res.startsWith("<diagram>"));

        assertThrows(ConfigurationException.class, ()->service.exportConfiguration("invalid_uuid"));
    }

    InputStreamResource getStream(String filename) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(filename);
        return new InputStreamResource(is);
    }
}
