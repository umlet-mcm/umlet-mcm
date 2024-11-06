package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.UxfService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files/uxf")
@RequiredArgsConstructor
public class UxfController {

    private final UxfService uxfService;
    private final ConfigurationDtoMapper configurationDtoMapper;

    @PostMapping
    public ResponseEntity<ConfigurationDTO> uploadUxfFile(@RequestParam("file") MultipartFile file) {
        var configuration = uxfService.createConfigurationFromUxf(file);
        var configurationDto = configurationDtoMapper.toDto(configuration);
        return ResponseEntity.ok(configurationDto);
    }
}
