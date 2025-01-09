package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/model")
@RequiredArgsConstructor
public class ModelController {
    private final ModelService modelService;

    @DeleteMapping("/{modelId}")
    public ResponseEntity<Void> deleteModel(@PathVariable final String modelId) {
        modelService.deleteModel(modelId);
        return ResponseEntity.noContent().build();
    }
}
