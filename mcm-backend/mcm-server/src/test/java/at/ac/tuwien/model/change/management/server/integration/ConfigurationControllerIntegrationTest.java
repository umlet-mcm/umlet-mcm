package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.git.config.GitProperties;
import jakarta.servlet.ServletContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigurationControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/configurations";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GitProperties gitProperties;

    @BeforeEach
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @BeforeEach
    public void setupGitProperties(@TempDir Path testDirectory) {
        // TODO: there should be a better way to do this
        gitProperties.setRepositories(testDirectory);
    }

    @Test
    public void testContext_shouldProvideConfigurationController() {
        ServletContext servletContext = webApplicationContext.getServletContext();
        Assertions.assertThat(servletContext).isNotNull();
        Assertions.assertThat(webApplicationContext.getBean("configurationController")).isNotNull();
    }

    @Test
    public void testCreate_nonExistingConfiguration_shouldSucceed() {
        var configurationName = "test";
        Assertions.assertThatCode(() -> mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + configurationName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(configurationName)))
                .doesNotThrowAnyException();
    }

    @Test
    public void testCreate_existingConfiguration_shouldThrowConfigurationAlreadyExistsException() {
        var configurationName = "test";
        Assertions.assertThatCode(() -> mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + configurationName + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(configurationName)))
                .doesNotThrowAnyException();

        Assertions.assertThatCode(() -> mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + configurationName + "\"}"))
                .andExpect(status().isConflict()))
                .doesNotThrowAnyException();
    }

    // TODO: add more tests for the other controller methods
}
