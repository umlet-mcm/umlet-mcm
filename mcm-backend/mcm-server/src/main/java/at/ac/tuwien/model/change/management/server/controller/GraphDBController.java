package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.GraphDBService;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.mapper.NodeDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/graphdb")
@RequiredArgsConstructor
public class GraphDBController {

    private final GraphDBService graphDBService;
    private final NodeDtoMapper nodeDtoMapper;

    @PostMapping(path = "/nodes/load")
    public ResponseEntity<NodeDTO> loadNode(@RequestBody NodeDTO nodeDTO) {
        var node = graphDBService.loadNode(nodeDtoMapper.fromDto(nodeDTO));
        return ResponseEntity.ok(nodeDtoMapper.toDto(node));
    }
}
