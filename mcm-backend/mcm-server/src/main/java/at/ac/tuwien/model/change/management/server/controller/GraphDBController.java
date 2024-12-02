package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.GraphDBService;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.dto.QueryDTO;
import at.ac.tuwien.model.change.management.server.mapper.NodeDtoMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(path = "/query")
    public ResponseEntity<String> executeQuery(@RequestBody QueryDTO query) {
        val result = graphDBService.executeQuery(query.query());
        Gson gson = new Gson();
        val convert = gson.toJson(result);
        return ResponseEntity.ok(convert);
    }
}
