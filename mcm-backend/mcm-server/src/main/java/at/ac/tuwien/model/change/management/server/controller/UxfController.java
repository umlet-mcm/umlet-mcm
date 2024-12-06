package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.service.ConfigurationNotFoundException;
import at.ac.tuwien.model.change.management.core.service.UxfService;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files/uxf")
@RequiredArgsConstructor
public class UxfController {

    private final UxfService uxfService;
    private final ConfigurationDtoMapper configurationDtoMapper;

    @PostMapping
    public ResponseEntity uploadUxfFile(@RequestParam("file") MultipartFile file) {
        try {
            Configuration configuration = uxfService.createConfigurationFromUxf(file);
            var configurationDto = configurationDtoMapper.toDto(configuration);
            return ResponseEntity.ok(configurationDto);
        } catch (UxfException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Failed to parse uxf: " + e.getMessage());
        }
    }

    @PostMapping("/{configId}")
    public ResponseEntity uploadUxfFile(@RequestParam("file") MultipartFile file, @PathVariable String configId) {
        try {
            Configuration res = uxfService.addUxfToConfiguration(file, configId);
            var configurationDto = configurationDtoMapper.toDto(res);
            return ResponseEntity.ok(configurationDto);
        } catch (ConfigurationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Configuration with id '" + configId + "' not found");
        } catch (ConfigurationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to get configuration '" + configId + "': " + e.getMessage());
        } catch (UxfException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Failed to parse uxf: " + e.getMessage());
        }
    }
}
