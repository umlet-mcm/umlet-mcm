package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.GraphDBService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.dto.QueryDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.NodeDtoMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/graphdb")
@RequiredArgsConstructor
public class GraphDBController {

    private final GraphDBService graphDBService;
    private final NodeDtoMapper nodeDtoMapper;
    private final ConfigurationDtoMapper configurationDtoMapper;

    @PostMapping(path = "/nodes/load")
    public ResponseEntity<NodeDTO> loadNode(@RequestBody NodeDTO nodeDTO) {
        var node = graphDBService.loadNode(nodeDtoMapper.fromDto(nodeDTO));
        return ResponseEntity.ok(nodeDtoMapper.toDto(node));
    }

    @PostMapping(path = "/configuration")
    public ResponseEntity<ConfigurationDTO> loadConfiguration(@RequestBody ConfigurationDTO configurationDTO) {
        var configuration = graphDBService.loadConfiguration(configurationDtoMapper.fromDto(configurationDTO));
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }

    @GetMapping(path = "/query")
    public ResponseEntity<String> executeQuery(@RequestBody QueryDTO query) {
        val result = graphDBService.executeQuery(query.query());
        Gson gson = new Gson();
        val convert = gson.toJson(result);
        return ResponseEntity.ok(convert);
    }

    @GetMapping(path = "/nodes/predecessors")
    public ResponseEntity<List<NodeDTO>> getPredecessors(
            @RequestParam String nodeID) {
        val nodes = graphDBService.getPredecessors(nodeID);
        return ResponseEntity.ok(nodes.stream().map(nodeDtoMapper::toDto).toList());
    }

    @GetMapping(path = "/nodes/sumUpAttribute")
    public ResponseEntity<NodeDTO> sumUpAttribute(
            @RequestParam String nodeID,
            @RequestParam String attributeName) {
        val node = graphDBService.sumUpAttribute(nodeID, attributeName);
        return ResponseEntity.ok(nodeDtoMapper.toDto(node));
    }
}
