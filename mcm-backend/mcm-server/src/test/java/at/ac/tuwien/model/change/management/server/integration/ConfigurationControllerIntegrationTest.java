package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.git.util.PathUtils;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationVersionDTO;
import at.ac.tuwien.model.change.management.server.dto.DiffDTO;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import at.ac.tuwien.model.change.management.server.testutil.ConfigurationDTOAssert;
import at.ac.tuwien.model.change.management.server.testutil.DtoGen;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
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

    private final static String TEST_CONFIGURATION_NAME = "test-configuration";

    @TempDir
    private static Path testDirectory;

    @DynamicPropertySource
    private static void setRepositoryPath(DynamicPropertyRegistry registry) {
        registry.add("app.git.repositories", () -> testDirectory.toString());
    }

    @AfterEach
    public void cleanup() throws IOException {
        PathUtils.deleteFilesRecursively(testDirectory, true);
    }

    @Test
    public void testGetConfiguration_existingConfiguration_shouldReturnConfigurationDTO() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
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
        ConfigurationDTOAssert.assertThat(foundConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testGetConfiguration_nonExistingConfiguration_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/non-existing"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllConfigurations_noExistingConfigurations_shouldReturnEmptyList() throws Exception {
        var result = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn();

        var foundConfigurations = deserialize(result, ConfigurationDTO[].class);
        Assertions.assertThat(foundConfigurations).isEmpty();
    }

    @Test
    public void testGetAllConfigurations_twoExistingConfigurations_shouldReturnBothConfigurationDTOs() throws Exception {
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

    @Test
    public void testCreateConfiguration_emptyConfiguration_shouldReturnConfiguration() throws Exception {
        var originalConfiguration = new ConfigurationDTO(TEST_CONFIGURATION_NAME, null, null);
        var configurationJson = jsonify(originalConfiguration);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TEST_CONFIGURATION_NAME))
                .andExpect(jsonPath("$.version").isNotEmpty())
                .andExpect(jsonPath("$.nodes").doesNotExist());
    }

    @Test
    public void testCreateConfiguration_configurationWithNodes_shouldReturnConfiguration() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(result, ConfigurationDTO.class);

        ConfigurationDTOAssert.assertThat(createdConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testCreateConfiguration_configurationAlreadyExists_shouldReturnConflict() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateConfiguration_configurationWithNodes_shouldReturnUpdatedConfiguration() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(result, ConfigurationDTO.class);
        var updatedConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, createdConfiguration.version().hash(), 2, 5, 0);
        var updatedConfigurationJson = jsonify(updatedConfiguration);

        var updateResult = mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfigurationResult = deserialize(updateResult, ConfigurationDTO.class);

        ConfigurationDTOAssert.assertThat(updatedConfigurationResult)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(updatedConfiguration);
    }

    @Test
    public void testDeleteConfiguration_existingConfiguration_shouldDeleteConfiguration() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
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
    public void testDeleteConfiguration_nonExistingConfiguration_shouldSucceedWithoutException() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/non-existing"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetConfigurationVersion_existingConfiguration_shouldReturnConfigurationDTO() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var resultFind = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash()))
                .andExpect(status().isOk())
                .andReturn();

        var foundConfiguration = deserialize(resultFind, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(foundConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testGetConfigurationVersion_nonExistingConfiguration_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/non-existing/versions/1.0.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetConfigurationVersion_configurationWithTwoVersions_shouldReturnOriginalConfigurationOnRequest() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 1, 2, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var newConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, createdConfiguration.version().hash(), 2, 5, 0);
        var newConfigurationJson = jsonify(newConfiguration);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();

        var resultFind = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash()))
                .andExpect(status().isOk())
                .andReturn();

        var foundConfiguration = deserialize(resultFind, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(foundConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testGetConfigurationVersion_configurationWithTwoVersions_shouldReturnUpdatedConfigurationOnRequest() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 1, 2, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var newConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, createdConfiguration.version().hash(), 2, 5, 0);
        var newConfigurationJson = jsonify(newConfiguration);

        var resultUpdate = mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfiguration = deserialize(resultUpdate, ConfigurationDTO.class);

        var resultFind = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + updatedConfiguration.version().hash()))
                .andExpect(status().isOk())
                .andReturn();

        var foundConfiguration = deserialize(resultFind, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(foundConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(newConfiguration);
    }

    @Test
    public void testListConfigurationVersions_nonExistingConfiguration_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + TEST_CONFIGURATION_NAME + "/versions"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testListConfigurationVersions_existingConfigurationWithVersion_shouldReturnVersion() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 0, 0, 0);
        var configurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var result = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions"))
                .andExpect(status().isOk())
                .andReturn();

        var foundVersions = deserialize(result, ConfigurationVersionDTO[].class);
        Assertions.assertThat(foundVersions).singleElement().extracting(ConfigurationVersionDTO::hash, STRING).hasSize(40);
        Assertions.assertThat(foundVersions).singleElement().extracting(ConfigurationVersionDTO::name, STRING).isEqualTo("v1.0.0");
        Assertions.assertThat(foundVersions).singleElement().extracting(ConfigurationVersionDTO::customName).isNull();
    }

    @Test
    public void testListConfigurationVersions_existingConfigurationWithTwoVersions_shouldReturnVersions() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 0, 0, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var newConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, createdConfiguration.version().hash(), 2, 5, 0);
        var newConfigurationJson = jsonify(newConfiguration);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();

        var result = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions"))
                .andExpect(status().isOk())
                .andReturn();

        var foundVersions = deserialize(result, ConfigurationVersionDTO[].class);
        Assertions.assertThat(foundVersions).hasSize(2)
                .allSatisfy(version -> Assertions.assertThat(version.hash()).hasSize(40));

        Assertions.assertThat(foundVersions).extracting(ConfigurationVersionDTO::name)
                .containsExactly("v1.0.1", "v1.0.0");

        Assertions.assertThat(foundVersions).extracting(ConfigurationVersionDTO::customName)
                .containsOnlyNulls();
    }

    @Test
    public void testCompareConfigurationVersions_sameVersion_includeUnchangedFalse_shouldReturnEmptyList() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 1, 2, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var diffResult = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash() + "/compare/" + createdConfiguration.version().hash()))
                .andExpect(status().isOk())
                .andReturn();

        var foundDiffs = deserialize(diffResult, ConfigurationDTO[].class);
        Assertions.assertThat(foundDiffs).isEmpty();
    }

    @Test
    public void testCompareConfigurationVersions_sameVersion_includeUnchangedTrue_shouldReturnUnchangedDiffs() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 1, 2, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var diffResult = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash() + "/compare/" + createdConfiguration.version().hash() + "?includeUnchanged=true"))
                .andExpect(status().isOk())
                .andReturn();

        var foundDiffs = deserialize(diffResult, DiffDTO[].class);
        Assertions.assertThat(foundDiffs).hasSize(3)
                .allMatch(diff -> diff.diffType().equals("UNCHANGED"));
    }

    @Test
    public void testCompareConfigurationVersions_addedElements_shouldReturnAddedDiffs() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 0, 0, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        // at the time of writing - minor problems in the ModelEntityMapper
        // avoid these by not loading into GraphDB
        var resultCreate = mockMvc.perform(post(BASE_URL + "?loadIntoGraphDB=false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var newConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, createdConfiguration.version().hash(), 2, 5, 0);
        var newConfigurationJson = jsonify(newConfiguration);

        var resultUpdate = mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfiguration = deserialize(resultUpdate, ConfigurationDTO.class);

        var diffResult = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + updatedConfiguration.version().hash() + "/compare/" + createdConfiguration.version().hash()))
                .andExpect(status().isOk())
                .andReturn();

        var foundDiffs = deserialize(diffResult, DiffDTO[].class);
        Assertions.assertThat(foundDiffs).hasSize(12)
                .allMatch(diff -> diff.diffType().equals("ADD"));
    }

    @Test
    public void testCompareConfiguration_modifiedElements_shouldReturnModifiedDiffs() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 5, 0, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL + "?loadIntoGraphDB=false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var modifiedModels = createdConfiguration.models().stream().map(modelDTO -> new ModelDTO(
                Collections.emptySet(),
                modelDTO.id(),
                modelDTO.tags(),
                modelDTO.title() + "-modified",
                modelDTO.description(),
                modelDTO.mcmAttributes(),
                modelDTO.mcmAttributesInlineComments(),
                modelDTO.zoomLevel()
        )).collect(Collectors.toSet());

        var newConfiguration = new ConfigurationDTO(
                createdConfiguration.name(),
                createdConfiguration.version(),
                modifiedModels
        );
        var newConfigurationJson = jsonify(newConfiguration);

        var resultUpdate = mockMvc.perform(put(BASE_URL + "?loadIntoGraphDB=false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfiguration = deserialize(resultUpdate, ConfigurationDTO.class);

        var diffResult = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + updatedConfiguration.version().hash() + "/compare/" + createdConfiguration.version().hash()))
                .andExpect(status().isOk())
                .andReturn();

        var foundDiffs = deserialize(diffResult, DiffDTO[].class);
        Assertions.assertThat(foundDiffs).hasSize(5)
                .allMatch(diff -> diff.diffType().equals("MODIFY"));
    }

    @Test
    public void testCompareConfiguration_removedElements_shouldReturnDeleteDiffs() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 3, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL + "?loadIntoGraphDB=false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var newConfiguration = new ConfigurationDTO(
                createdConfiguration.name(),
                createdConfiguration.version(),
                Collections.emptySet()
        );
        var newConfigurationJson = jsonify(newConfiguration);

        var resultUpdate = mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfiguration = deserialize(resultUpdate, ConfigurationDTO.class);

        var diffResult = mockMvc.perform(get(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + updatedConfiguration.version().hash() + "/compare/" + createdConfiguration.version().hash()))
                .andExpect(status().isOk())
                .andReturn();

        var foundDiffs = deserialize(diffResult, DiffDTO[].class);
        Assertions.assertThat(foundDiffs).hasSize(8)
                .allMatch(diff -> diff.diffType().equals("DELETE"));
    }

    @Test
    public void testCheckoutConfiguration_nonExistingConfiguration_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(BASE_URL + "/non-existing/versions/1.0.0/checkout"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCheckoutConfiguration_existingConfiguration_shouldReturnConfigurationDTO() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var resultCheckout = mockMvc.perform(post(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash() + "/checkout"))
                .andExpect(status().isOk())
                .andReturn();

        var checkedOutConfiguration = deserialize(resultCheckout, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(checkedOutConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testCheckoutConfiguration_existingConfigurationWithTwoVersions_shouldCheckoutOriginalConfigurationUponRequest() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 1, 2, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var newConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, createdConfiguration.version().hash(), 2, 5, 0);
        var newConfigurationJson = jsonify(newConfiguration);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();

        var resultCheckout = mockMvc.perform(post(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash() + "/checkout"))
                .andExpect(status().isOk())
                .andReturn();

        var checkedOutConfiguration = deserialize(resultCheckout, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(checkedOutConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testReset_nonExistingConfiguration_shouldReturnNotFound() throws Exception {
        mockMvc.perform(post(BASE_URL + "/non-existing/versions/1.0.0/reset"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testReset_existingConfiguration_shouldReturnConfigurationDTO() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 2, 5, 0);
        var configurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var resultReset = mockMvc.perform(post(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash() + "/reset"))
                .andExpect(status().isOk())
                .andReturn();

        var resetConfiguration = deserialize(resultReset, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(resetConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testReset_existingConfigurationWithTwoVersions_shouldResetToOriginalConfigurationUponRequest() throws Exception {
        var originalConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, 1, 2, 0);
        var originalConfigurationJson = jsonify(originalConfiguration);

        var resultCreate = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(resultCreate, ConfigurationDTO.class);

        var newConfiguration = DtoGen.generateRandomizedConfigurationDTO(TEST_CONFIGURATION_NAME, createdConfiguration.version().hash(), 2, 5, 0);
        var newConfigurationJson = jsonify(newConfiguration);

        mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();

        var resultReset = mockMvc.perform(post(BASE_URL + "/" + createdConfiguration.name() + "/versions/" + createdConfiguration.version().hash() + "/reset"))
                .andExpect(status().isOk())
                .andReturn();

        var resetConfiguration = deserialize(resultReset, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(resetConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .containsSameElementsAs(originalConfiguration);
    }

    @Test
    public void testCreateConfiguration_withCustomVersionName_shouldReturnConfigurationDTO() throws Exception {
        var originalConfiguration = new ConfigurationDTO(
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersionDTO(null, null, "custom-name"),
                null
        );
        var configurationJson = jsonify(originalConfiguration);

        var response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();

        var responseConfiguration = deserialize(response, ConfigurationDTO.class);
        ConfigurationDTOAssert.assertThat(responseConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .hasVersionCustomName("custom-name");
    }

    @Test
    public void testUpdateConfiguration_withCustomVersionName_shouldReturnConfigurationDTO() throws Exception {
        var originalConfiguration = new ConfigurationDTO(
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersionDTO(null, null, null),
                null
        );
        var configurationJson = jsonify(originalConfiguration);

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(result, ConfigurationDTO.class);
        var updatedConfiguration = new ConfigurationDTO(
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersionDTO(createdConfiguration.version().hash(), null, "custom-name"),
                null
        );
        var updatedConfigurationJson = jsonify(updatedConfiguration);

        var updateResult = mockMvc.perform(put(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedConfigurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfigurationResult = deserialize(updateResult, ConfigurationDTO.class);

        ConfigurationDTOAssert.assertThat(updatedConfigurationResult)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasValidVersion()
                .hasVersionCustomName("custom-name");
    }

    @Test
    public void testRenameConfiguration_existingConfiguration_shouldReturnConfigurationDTO() throws Exception {
        var originalConfiguration = new ConfigurationDTO(
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersionDTO(null, null, null),
                null
        );
        var configurationJson = jsonify(originalConfiguration);

        var result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(configurationJson))
                .andExpect(status().isOk())
                .andReturn();
        var createdConfiguration = deserialize(result, ConfigurationDTO.class);

        var updateResult = mockMvc.perform(put(BASE_URL + "/" + createdConfiguration.name() + "/rename?newName=updated-name")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        var updatedConfigurationResult = deserialize(updateResult, ConfigurationDTO.class);

        ConfigurationDTOAssert.assertThat(updatedConfigurationResult)
                .hasName("updated-name");
    }

    @SneakyThrows(JsonProcessingException.class)
    private static String jsonify(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows({UnsupportedEncodingException.class, JsonProcessingException.class})
    private static <T> T deserialize(MvcResult result, Class<T> clazz) {
        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }
}
