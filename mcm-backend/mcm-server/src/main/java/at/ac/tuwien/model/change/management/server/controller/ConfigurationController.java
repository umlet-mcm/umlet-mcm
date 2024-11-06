package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.ConfigurationService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final ConfigurationDtoMapper configurationDtoMapper;

    @PostMapping
    public ResponseEntity<ConfigurationDTO> createConfiguration(@RequestBody ConfigurationDTO dto) {
        var createdConfiguration = configurationService.create(configurationDtoMapper.fromDto(dto));
        return ResponseEntity.ok(configurationDtoMapper.toDto(createdConfiguration));
    }
}
