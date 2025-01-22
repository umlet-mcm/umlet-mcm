package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.ConfigurationService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationVersionDTO;
import at.ac.tuwien.model.change.management.server.dto.DiffDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationVersionDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.DiffDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final ConfigurationDtoMapper configurationDtoMapper;
    private final ConfigurationVersionDtoMapper configurationVersionDtoMapper;
    private final DiffDtoMapper diffDtoMapper;

    /**
     * Get the most recent version of a configuration by its name
     *
     * @param name            the name of the configuration
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the most recent version of the configuration with the given name
     */
    @GetMapping("/{name}")
    public ResponseEntity<ConfigurationDTO> getConfiguration(
            @PathVariable String name,
            @RequestParam(defaultValue = "false") boolean loadIntoGraphDB
    ) {
        var configuration = configurationService.getConfigurationByName(name, loadIntoGraphDB);
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }

    /**
     * Get the most recent version of all configurations
     *
     * @return a list of the most recent versions of all configurations
     */
    @GetMapping
    public ResponseEntity<List<ConfigurationDTO>> getAllConfigurations() {
        var configurations = configurationService.getAllConfigurations();
        return ResponseEntity.ok(configurationDtoMapper.toDto(configurations));
    }

    /**
     * Create a new configuration
     *
     * @param dto             the configuration to create
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the created configuration
     */
    @PostMapping
    public ResponseEntity<ConfigurationDTO> createConfiguration(
            @RequestBody ConfigurationDTO dto,
            @RequestParam(defaultValue = "true") boolean loadIntoGraphDB
    ) {
        var createdConfiguration = configurationService.createConfiguration(configurationDtoMapper.fromDto(dto), loadIntoGraphDB);
        return ResponseEntity.ok(configurationDtoMapper.toDto(createdConfiguration));
    }

    /**
     * Update an existing configuration
     *
     * @param dto             the configuration to update - should include the version string of the most recent configuration
     *                        version which we are trying to update
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the updated configuration
     */
    @PutMapping
    public ResponseEntity<ConfigurationDTO> updateConfiguration(
            @RequestBody ConfigurationDTO dto,
            @RequestParam(defaultValue = "true") boolean loadIntoGraphDB
    ) {
        var updatedConfiguration = configurationService.updateConfiguration(configurationDtoMapper.fromDto(dto), loadIntoGraphDB);
        return ResponseEntity.ok(configurationDtoMapper.toDto(updatedConfiguration));
    }

    /**
     * Delete a configuration by its name
     *
     * @param name the name of the configuration to delete
     * @return a response entity with no content
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable String name) {
        configurationService.deleteConfiguration(name);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get a specific version of a configuration by its name and version
     *
     * @param name            the name of the configuration
     * @param version         the version of the configuration
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the configuration with the given name and version
     */
    @GetMapping("/{name}/versions/{version}")
    public ResponseEntity<ConfigurationDTO> getConfigurationVersion(
            @PathVariable String name,
            @PathVariable String version,
            @RequestParam(defaultValue = "false") boolean loadIntoGraphDB
    ) {
        var configuration = configurationService.getConfigurationVersion(name, version, loadIntoGraphDB);
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }

    /**
     * List all versions of a configuration by its name
     *
     * @param name the name of the configuration
     * @return a list of all versions of the configuration with the given name
     */
    @GetMapping("/{name}/versions")
    public ResponseEntity<List<ConfigurationVersionDTO>> listConfigurationVersions(@PathVariable String name) {
        var versions = configurationVersionDtoMapper.toDto(configurationService.listConfigurationVersions(name));
        return ResponseEntity.ok(versions);
    }

    /**
     * Compare two versions of a configuration by their names
     * Produces `git diff` / unified diff style output
     *
     * @param name             the name of the configuration
     * @param newVersion       the new version to compare with
     * @param oldVersion       the old version to compare with
     * @param includeUnchanged whether to include unchanged models, nodes or relations in the comparison results
     *                         if set to true, these will be included as "UNCHANGED" diff entries with the content simply
     *                         being the XML representation of the model, node or relation (i.e., not including any Git headers or hunks)
     * @return a list of differences between the two versions of the configuration
     */
    @GetMapping("/{name}/versions/{newVersion}/compare/{oldVersion}")
    public ResponseEntity<List<DiffDTO>> compareConfigurationVersions(
            @PathVariable String name,
            @PathVariable String newVersion,
            @PathVariable String oldVersion,
            @RequestParam(defaultValue = "false") boolean includeUnchanged
    ) {
        var diffs = configurationService.compareConfigurationVersions(name, oldVersion, newVersion, includeUnchanged);
        return ResponseEntity.ok(diffDtoMapper.toDto(diffs));
    }

    /**
     * Checkout a specific version of a configuration by its name
     *
     * @param name            the name of the configuration
     * @param version         the version to `git checkout`
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the configuration with the given name and version
     */
    @PostMapping("/{name}/versions/{version}/checkout")
    public ResponseEntity<ConfigurationDTO> checkoutConfiguration(
            @PathVariable String name,
            @PathVariable String version,
            @RequestParam(defaultValue = "false") boolean loadIntoGraphDB
    ) {
        var configuration = configurationService.checkoutConfigurationVersion(name, version, loadIntoGraphDB);
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }

    /**
     * Reset a specific version of a configuration by its name
     *
     * @param name            the name of the configuration
     * @param version         the version to reset to
     * @param loadIntoGraphDB whether the configuration should also automatically be loaded into the graph database
     * @return the configuration with the given name and version
     */
    @PostMapping("/{name}/versions/{version}/reset")
    public ResponseEntity<ConfigurationDTO> resetConfiguration(
            @PathVariable String name,
            @PathVariable String version,
            @RequestParam(defaultValue = "false") boolean loadIntoGraphDB
    ) {
        var configuration = configurationService.resetConfigurationVersion(name, version, loadIntoGraphDB);
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }

    /**
     * Rename a configuration by its name
     *
     * @param name    the name of the configuration to rename
     * @param newName the new name of the configuration
     * @return the renamed configuration
     */
    @PutMapping("/{name}/rename")
    public ResponseEntity<ConfigurationDTO> renameConfiguration(
            @PathVariable String name,
            @RequestParam String newName
    ) {
        var configuration = configurationService.renameConfiguration(name, newName);
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }
}
