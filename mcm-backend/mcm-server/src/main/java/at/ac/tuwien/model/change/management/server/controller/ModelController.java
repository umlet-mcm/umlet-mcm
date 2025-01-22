package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.service.ModelService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.ModelDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/model")
@RequiredArgsConstructor
public class ModelController {
    private final ModelService modelService;
    private final ModelDtoMapper modelDtoMapper;
    private final ConfigurationDtoMapper configurationDtoMapper;

    @DeleteMapping("/{modelId}")
    public ResponseEntity<ConfigurationDTO> deleteModel(@PathVariable final String modelId) {
        var updatedConf = modelService.deleteModel(modelId);
        return ResponseEntity.ok(configurationDtoMapper.toDto(updatedConf));
    }

    @PostMapping("/alignModels")
    public ResponseEntity<List<ModelDTO>> alignModels(@RequestBody List<ModelDTO> modelDTOs) {
        List<Model> models = modelDTOs.stream().map(modelDtoMapper::fromDto).toList();
        List<Model> alignedModels = modelService.alignModels(models);
        List<ModelDTO> alignedModelDTOs = alignedModels.stream().map(modelDtoMapper::toDto).toList();
        return ResponseEntity.ok(alignedModelDTOs);
    }
}
