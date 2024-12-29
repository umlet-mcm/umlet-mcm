package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.service.ConfigurationNotFoundException;
import at.ac.tuwien.model.change.management.core.service.UxfService;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

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
                    .body("Failed to get configuration '" + configId + "'");
        } catch (UxfException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Failed to parse uxf");
        }
    }

    @GetMapping("export/configuration/{configUuid}")
    public ResponseEntity exportConfiguration(@PathVariable String configUuid) {
        try {
            String configXml = uxfService.exportConfiguration(configUuid);
            byte[] xmlBytes = configXml.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=configuration_" + configUuid + ".uxf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
            return new ResponseEntity<>(xmlBytes, headers, HttpStatus.OK);
        } catch (ConfigurationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Configuration with id '" + configUuid + "' not found");
        } catch (ConfigurationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to get configuration '" + configUuid + "'");
        } catch (UxfException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Failed to export configuration to uxf");
        }
    }

    @GetMapping("export/model/{modelUuid}")
    public ResponseEntity exportModel(@PathVariable String modelUuid) {
        try {
            String modelXml = uxfService.exportModel(modelUuid);
            byte[] xmlBytes = modelXml.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=model_" + modelUuid + ".uxf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
            return new ResponseEntity<>(xmlBytes, headers, HttpStatus.OK);
        } catch (ModelNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Model with id '" + modelUuid + "' not found");
        } catch (ConfigurationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to get configurations");
        } catch (UxfException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Failed to export model to uxf");
        }
    }
}
