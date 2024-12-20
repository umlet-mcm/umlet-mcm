package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.git.config.GitProperties;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.testutil.DtoGen;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ConfigurationControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/configurations";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GitProperties gitProperties;

    @BeforeEach
    public void setupGitProperties(@TempDir Path testDirectory) {
        // TODO: there should be a better way to do this
        gitProperties.setRepositories(testDirectory);
    }

    @Test
    public void testCreate_emptyConfiguration_shouldSucceed() throws Exception {
        var originalConfiguration = new ConfigurationDTO("test", null, null);
        var configurationJson = jsonify(originalConfiguration);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(originalConfiguration.name()))
                .andExpect(jsonPath("$.version").isNotEmpty())
                .andExpect(jsonPath("$.nodes").doesNotExist());
    }

    @Test
    public void testCreate_configurationWithNodes_shouldSucceed() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO("test", 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(result, ConfigurationDTO.class);

        Assertions.assertThat(createdConfiguration.version()).isNotNull();
        Assertions.assertThat(createdConfiguration)
                .usingRecursiveComparison(recursiveConfigurationDtoComparison())
                .isEqualTo(originalConfiguration);
    }

    @Test
    public void testUpdate_configurationWithNodes_shouldSucceed() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO("test", 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(result, ConfigurationDTO.class);
        var updatedConfiguration = DtoGen.generateRandomizedConfigurationDTO("test", createdConfiguration.version(), 2, 5, 0);
        var updatedConfigurationJson = jsonify(updatedConfiguration);

        var updateResult = mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfigurationResult = deserialize(updateResult, ConfigurationDTO.class);

        Assertions.assertThat(updatedConfigurationResult.version()).isNotNull();
        Assertions.assertThat(updatedConfigurationResult)
                .usingRecursiveComparison(recursiveConfigurationDtoComparison())
                .isEqualTo(updatedConfiguration);
    }


    @Test
    public void testGetByName_existingConfiguration_shouldSucceed() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO("test", 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var resultFind = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name()))
                .andExpect(status().isOk())
                .andReturn();

        var foundConfiguration = deserialize(resultFind, ConfigurationDTO.class);
        Assertions.assertThat(foundConfiguration.version()).isNotNull();
        Assertions.assertThat(foundConfiguration)
                .usingRecursiveComparison(recursiveConfigurationDtoComparison())
                .isEqualTo(originalConfiguration);
    }

    @Test
    public void testGetByName_nonExistingConfiguration_shouldFail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/non-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDelete_existingConfiguration_shouldSucceed() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO("test", 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        mockMvc.perform(delete(BASE_URL + "/" + createdConfiguration.name()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDelete_nonExistingConfiguration_shouldSucceed() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/non-existing"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetAll_existingConfigurations_shouldSucceed() throws Exception {
        var originalConfiguration1 = DtoGen.generateRandomizedConfigurationDTO("test1", 2, 5, 0);
        var configurationJson1 = jsonify(originalConfiguration1);

        var resultCreate1 = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson1))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration1 = deserialize(resultCreate1, ConfigurationDTO.class);

        var originalConfiguration2 = DtoGen.generateRandomizedConfigurationDTO("test2", 1, 2, 0);
        var configurationJson2 = jsonify(originalConfiguration2);

        var resultCreate2 = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson2))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration2 = deserialize(resultCreate2, ConfigurationDTO.class);

        var resultFindAll = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn();

        var foundConfigurations = deserialize(resultFindAll, ConfigurationDTO[].class);
        Assertions.assertThat(foundConfigurations).hasSize(2)
                .containsExactlyInAnyOrder(createdConfiguration1, createdConfiguration2);
    }

    @SneakyThrows
    private static String jsonify(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    private static <T> T deserialize(MvcResult result, Class<T> clazz) {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    private static RecursiveComparisonConfiguration recursiveConfigurationDtoComparison() {
        var recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.ignoreFields(
                "version",
                "models.id",
                "models.nodes.id",
                "models.nodes.mcmModelId",
                "models.nodes.relations.id",
                "models.nodes.relations.mdmModelId"
        );
        return recursiveComparisonConfiguration;
    }
}
