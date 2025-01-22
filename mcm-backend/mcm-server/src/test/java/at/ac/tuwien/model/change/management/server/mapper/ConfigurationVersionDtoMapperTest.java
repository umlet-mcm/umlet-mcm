package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.ConfigurationVersion;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationVersionDTO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

public class ConfigurationVersionDtoMapperTest {

    private final ConfigurationVersionDtoMapper configurationVersionDtoMapper = Mappers.getMapper(ConfigurationVersionDtoMapper.class);

    @Test
    public void testToDto_emptyList_shouldReturnEmptyList() {
        List<ConfigurationVersion> configurationVersions = Collections.emptyList();
        List<ConfigurationVersionDTO> result = configurationVersionDtoMapper.toDto(configurationVersions);
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void testToDto_elementWithNullValues_shouldReturnElementWithNullValues() {
        ConfigurationVersion configurationVersion = new ConfigurationVersion(null, null, null);
        List<ConfigurationVersion> configurationVersions = Collections.singletonList(configurationVersion);
        List<ConfigurationVersionDTO> result = configurationVersionDtoMapper.toDto(configurationVersions);
        Assertions.assertThat(result)
                .singleElement()
                .extracting(ConfigurationVersionDTO::hash, ConfigurationVersionDTO::name, ConfigurationVersionDTO::customName)
                .containsOnlyNulls();
    }

    @Test
    public void testToDto_elementWithValues_shouldReturnElementWithValues() {
        ConfigurationVersion configurationVersion = new ConfigurationVersion("hash", "name", "customName");
        List<ConfigurationVersion> configurationVersions = Collections.singletonList(configurationVersion);
        List<ConfigurationVersionDTO> result = configurationVersionDtoMapper.toDto(configurationVersions);
        Assertions.assertThat(result)
                .singleElement()
                .extracting(ConfigurationVersionDTO::hash, ConfigurationVersionDTO::name, ConfigurationVersionDTO::customName)
                .containsExactly(configurationVersion.hash(), configurationVersion.name(), configurationVersion.customName());
    }

    @Test
    public void tesToDto_elementWithOnlyHashes_shouldReturnElementWithOnlyHashes() {
        ConfigurationVersion configurationVersion = new ConfigurationVersion("hash", null, null);
        List<ConfigurationVersion> configurationVersions = Collections.singletonList(configurationVersion);
        List<ConfigurationVersionDTO> result = configurationVersionDtoMapper.toDto(configurationVersions);
        Assertions.assertThat(result)
                .singleElement()
                .extracting(ConfigurationVersionDTO::hash, ConfigurationVersionDTO::name, ConfigurationVersionDTO::customName)
                .containsExactly(configurationVersion.hash(), null, null);
    }

    @Test
    public void tesToDto_elementWithOnlyNames_shouldReturnElementWithOnlyNames() {
        ConfigurationVersion configurationVersion = new ConfigurationVersion(null, "name", null);
        List<ConfigurationVersion> configurationVersions = Collections.singletonList(configurationVersion);
        List<ConfigurationVersionDTO> result = configurationVersionDtoMapper.toDto(configurationVersions);
        Assertions.assertThat(result)
                .singleElement()
                .extracting(ConfigurationVersionDTO::hash, ConfigurationVersionDTO::name, ConfigurationVersionDTO::customName)
                .containsExactly(null, configurationVersion.name(), null);
    }

    @Test
    public void tesToDto_elementWithOnlyCustomNames_shouldReturnElementWithOnlyCustomNames() {
        ConfigurationVersion configurationVersion = new ConfigurationVersion(null, null, "customName");
        List<ConfigurationVersion> configurationVersions = Collections.singletonList(configurationVersion);
        List<ConfigurationVersionDTO> result = configurationVersionDtoMapper.toDto(configurationVersions);
        Assertions.assertThat(result)
                .singleElement()
                .extracting(ConfigurationVersionDTO::hash, ConfigurationVersionDTO::name, ConfigurationVersionDTO::customName)
                .containsExactly(null, null, configurationVersion.customName());
    }

    @Test
    public void testToDto_threeElementsWithValues_shouldReturnThreeElementsWithValues() {
        ConfigurationVersion configurationVersion1 = new ConfigurationVersion("hash1", "name1", "customName1");
        ConfigurationVersion configurationVersion2 = new ConfigurationVersion("hash2", "name2", "customName2");
        ConfigurationVersion configurationVersion3 = new ConfigurationVersion("hash3", "name3", "customName3");
        List<ConfigurationVersion> configurationVersions = List.of(configurationVersion1, configurationVersion2, configurationVersion3);
        List<ConfigurationVersionDTO> result = configurationVersionDtoMapper.toDto(configurationVersions);
        Assertions.assertThat(result)
                .hasSize(3)
                .extracting(ConfigurationVersionDTO::hash, ConfigurationVersionDTO::name, ConfigurationVersionDTO::customName)
                .containsExactly(
                        Assertions.tuple(configurationVersion1.hash(), configurationVersion1.name(), configurationVersion1.customName()),
                        Assertions.tuple(configurationVersion2.hash(), configurationVersion2.name(), configurationVersion2.customName()),
                        Assertions.tuple(configurationVersion3.hash(), configurationVersion3.name(), configurationVersion3.customName())
                );
    }
}
