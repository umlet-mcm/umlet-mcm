package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.configuration.JaxbConfig;
import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.transformer.XMLTransformerImpl;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.git.repository.VersionControlRepository;
import at.ac.tuwien.model.change.management.testutil.MockConfigurationRepository;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class UxfServiceTest {
    private ConfigurationRepository configurationRepository;
    private AutoCloseable annotations;

    @Mock
    private VersionControlRepository versionControlRepository;

    private ConfigurationService configurationService;

    @Mock
    private GraphDBService graphDBService;

    @Mock
    private NameValidationService nameValidationService;

    private UxfServiceImpl service;

    @BeforeEach
    public void setup() throws JAXBException {
        configurationRepository = new MockConfigurationRepository();
        annotations = MockitoAnnotations.openMocks(this);
        configurationService = new ConfigurationServiceImpl(configurationRepository, versionControlRepository, graphDBService, nameValidationService);

        JAXBContext jaxbContext = new JaxbConfig().jaxbContext();

        var xmlTransformer = new XMLTransformerImpl(jaxbContext);
        service = new UxfServiceImpl(configurationService, xmlTransformer);
    }

    @AfterEach
    public void teardown() throws Exception {
        annotations.close();
    }

    @Test
    void testCreateConfigurationFromUxf() throws UxfException {
        String in = "uxf/frag1_cost_updated.uxf";
        assertDoesNotThrow(() -> service.createConfigurationFromUxf(getStream(in), null));
        Configuration res = service.createConfigurationFromUxf(getStream(in), null);
        assertEquals(1, res.getModels().size());
        assertEquals(7, res.getModels().stream()
                .findFirst()
                .orElseThrow()
                .getNodes().size());
    }

    @Test
    void testAddUxfToConfiguration() throws UxfException {
        when(versionControlRepository.getCurrentVersion(anyString())).thenAnswer(invocation ->
                Optional.of(findVersionByName(invocation.getArgument(0))));

        String in1 = "uxf/frag1_cost_updated.uxf";
        Configuration target = service.createConfigurationFromUxf(getStream(in1), null);

        String in2 = "uxf/frag2.uxf";
        Configuration res = service.addUxfToConfiguration(getStream(in2), target.getName(), null);

        assertEquals(2, res.getModels().size());

        assertThrows(ConfigurationException.class, () -> service.addUxfToConfiguration(getStream(in2), "invalid_uuid", null));
    }

    @Test
    void testExportModel() throws UxfException, ModelNotFoundException {
        String in = "uxf/frag1_cost_updated.uxf";
        Configuration conf = service.createConfigurationFromUxf(getStream(in), null);
        String targetId = conf.getModels().stream().findFirst().orElseThrow().getId();

        assertDoesNotThrow(() -> service.exportModel(conf.getName(), targetId));
        String res = service.exportModel(conf.getName(), targetId);
        assertNotNull(res);
        assertTrue(res.startsWith("<diagram>"));

        assertThrows(ConfigurationNotFoundException.class, () -> service.exportModel("invalid conf","invalid_uuid"));
        assertThrows(ModelNotFoundException.class, () -> service.exportModel(conf.getName(),"invalid_uuid"));
    }

    @Test
    void testExportConfiguration() throws UxfException, ConfigurationException {
        String in = "uxf/frag1_cost_updated.uxf";
        Configuration conf = service.createConfigurationFromUxf(getStream(in), null);

        assertDoesNotThrow(() -> service.exportConfiguration(conf.getName()));
        String res = service.exportConfiguration(conf.getName());
        assertNotNull(res);
        assertTrue(res.startsWith("<diagram>"));

        assertThrows(ConfigurationException.class, () -> service.exportConfiguration("invalid_uuid"));
    }

    InputStreamResource getStream(String filename) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(filename);
        return new InputStreamResource(is);
    }

    private String findVersionByName(String name) {
        return configurationRepository.findCurrentVersionOfConfigurationByName(name).orElseThrow().getVersionHash();
    }
}
