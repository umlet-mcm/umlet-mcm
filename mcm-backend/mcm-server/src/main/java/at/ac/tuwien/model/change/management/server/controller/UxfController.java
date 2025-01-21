package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.service.UxfService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.ModelDtoMapper;
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
    private final ModelDtoMapper modelDtoMapper;

    @PostMapping
    public ResponseEntity<ConfigurationDTO> createNewConfigurationFromUxf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String version
    ) {
        try {
            Configuration configuration = uxfService.createConfigurationFromUxf(file, name, version);
            var configurationDto = configurationDtoMapper.toDto(configuration);
            return ResponseEntity.ok(configurationDto);
        } catch (UxfException e) {
            throw new UxfParsingException("Failed to parse uxf: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{configName}")
    public ResponseEntity<ConfigurationDTO> updateConfigurationFromUxf(
            @RequestParam("file") MultipartFile file,
            @PathVariable String configName,
            @RequestParam(required = false) String version
    ) {
        try {
            Configuration configuration = uxfService.updateConfigurationFromUxf(file, configName, version);
            var configurationDto = configurationDtoMapper.toDto(configuration);
            return ResponseEntity.ok(configurationDto);
        } catch (UxfException e) {
            throw new UxfParsingException("Failed to parse uxf: " + e.getMessage(), e);
        }

    }

    @PostMapping("/{configId}")
    public ResponseEntity<ConfigurationDTO> uploadUxfFileToConfiguration(@RequestParam("file") MultipartFile file,
                                                                         @PathVariable String configId,
                                                                         @RequestParam(required = false) String modelName) {
        try {
            // todo update here
            Configuration res = uxfService.addUxfToConfiguration(file, configId, modelName);
            var configurationDto = configurationDtoMapper.toDto(res);
            return ResponseEntity.ok(configurationDto);
        } catch (UxfException e) {
            throw new UxfParsingException("Failed to parse uxf: " + e.getMessage(), e);
        }
    }

    @GetMapping("export/configuration/{configUuid}")
    public ResponseEntity<byte[]> exportConfiguration(@PathVariable String configUuid) {
        try {
            String configXml = uxfService.exportConfiguration(configUuid);
            byte[] xmlBytes = configXml.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=configuration_" + configUuid + ".uxf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
            return new ResponseEntity<>(xmlBytes, headers, HttpStatus.OK);
        } catch (UxfException e) {
            throw new UxfExportException("Failed to export configuration to uxf", e);
        }
    }

    @GetMapping("export/configuration/{configName}/model/{modelUuid}")
    public ResponseEntity<byte[]> exportModel(@PathVariable String configName, @PathVariable String modelUuid) {
        try {
            String modelXml = uxfService.exportModel(configName, modelUuid);
            byte[] xmlBytes = modelXml.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=model_" + modelUuid + ".uxf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
            return new ResponseEntity<>(xmlBytes, headers, HttpStatus.OK);
        } catch (UxfException e) {
            throw new UxfExportException("Failed to export model to uxf", e);
        }
    }

    @PutMapping
    public ResponseEntity<ModelDTO> updateModel(@RequestParam("file") MultipartFile file,
                                                @RequestParam("newModelName") String newModelName) {
        try {
            Model updatedModel = uxfService.updateModelFromUxf(file, newModelName);
            return ResponseEntity.ok(modelDtoMapper.toDto(updatedModel));
        } catch (UxfException e) {
            throw new UxfParsingException("Failed to parse uxf: " + e.getMessage(), e);
        }
    }
}
