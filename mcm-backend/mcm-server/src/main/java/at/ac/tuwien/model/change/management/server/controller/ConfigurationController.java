package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.ConfigurationService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
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

    @GetMapping("/{name}")
    public ResponseEntity<ConfigurationDTO> getConfiguration(@PathVariable String name) {
        var configuration = configurationService.getConfigurationByName(name);
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }

    @GetMapping
    public ResponseEntity<List<ConfigurationDTO>> getAllConfigurations() {
        var configurations = configurationService.getAllConfigurations();
        return ResponseEntity.ok(configurationDtoMapper.toDto(configurations));
    }

    @PostMapping
    public ResponseEntity<ConfigurationDTO> createConfiguration(@RequestBody ConfigurationDTO dto) {
        var createdConfiguration = configurationService.createConfiguration(configurationDtoMapper.fromDto(dto));
        return ResponseEntity.ok(configurationDtoMapper.toDto(createdConfiguration));
    }

    @PutMapping
    public ResponseEntity<ConfigurationDTO> updateConfiguration(@RequestBody ConfigurationDTO dto) {
        var updatedConfiguration = configurationService.updateConfiguration(configurationDtoMapper.fromDto(dto));
        return ResponseEntity.ok(configurationDtoMapper.toDto(updatedConfiguration));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable String name) {
        configurationService.deleteConfiguration(name);
        return ResponseEntity.noContent().build();
    }
}
