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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing graph database operations.
 */
@RestController
@RequestMapping("/api/v1/graphdb")
@RequiredArgsConstructor
public class GraphDBController {

    private final GraphDBService graphDBService;
    private final NodeDtoMapper nodeDtoMapper;
    private final ConfigurationDtoMapper configurationDtoMapper;

    /**
     * Loads a node into the graph database.
     * @param nodeDTO the node data transfer object
     * @return the loaded node as a data transfer object
     */
    @PostMapping(path = "/nodes/load")
    public ResponseEntity<NodeDTO> loadNode(@RequestBody NodeDTO nodeDTO) {
        var node = graphDBService.loadNode(nodeDtoMapper.fromDto(nodeDTO));
        return ResponseEntity.ok(nodeDtoMapper.toDto(node));
    }

    /**
     * Deletes a node from the graph database.
     * @param nodeID the ID of the node to delete
     */
    @DeleteMapping(path = "/nodes/{nodeID}")
    public ResponseEntity<Void> deleteNode(@PathVariable("nodeID") String nodeID) {
        graphDBService.deleteNode(nodeID);
        return ResponseEntity.ok().build();
    }

    /**
     * Loads a configuration into the graph database.
     * @param configurationDTO the configuration data transfer object
     * @return the loaded configuration as a data transfer object
     */
    @PostMapping(path = "/configuration")
    public ResponseEntity<ConfigurationDTO> loadConfiguration(@RequestBody ConfigurationDTO configurationDTO) {
        var configuration = graphDBService.loadConfiguration(configurationDtoMapper.fromDto(configurationDTO));
        return ResponseEntity.ok(configurationDtoMapper.toDto(configuration));
    }

    /**
     * Deletes a configuration from the graph database.
     * @param configurationID the ID of the configuration to delete
     */
    @DeleteMapping(path = "/configuration/{configurationID}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable("configurationID") String configurationID) {
        graphDBService.deleteConfiguration(configurationID);
        return ResponseEntity.ok().build();
    }

    /**
     * Clears the entire graph database.
     * @return a response entity with a success message
     */
    @DeleteMapping
    public ResponseEntity<String> clearDatabase() {
        graphDBService.clearDatabase();
        return ResponseEntity.ok("Successfully cleared the DB!");
    }

    /**
     * Executes a custom query on the graph database. (including currently attached libraries)
     * @param query the cypher query for database
     * @return the query result as a JSON string
     */
    @GetMapping(path = "/query")
    public ResponseEntity<String> executeQuery(@RequestBody QueryDTO query) {
        val result = graphDBService.executeQuery(query.query());
        Gson gson = new Gson();
        val convert = gson.toJson(result);
        return ResponseEntity.ok(convert);
    }

    /**
     * Retrieves the predecessors of a node
     * @param nodeID the ID of the node
     * @return a list of predecessor nodes
     */
    @GetMapping(path = "/nodes/predecessors")
    public ResponseEntity<List<NodeDTO>> getPredecessors(
            @RequestParam String nodeID) {
        val nodes = graphDBService.getPredecessors(nodeID);
        return ResponseEntity.ok(nodes.stream().map(nodeDtoMapper::toDto).toList());
    }

    /**
     * Sum up one given attribute from all predecessors of a node
     * @param nodeID The node ID which will be summed up
     * @param attributeName The attribute name to sum up
     * @return The node containing the summed attribute
     */
    @GetMapping(path = "/nodes/sumUpAttribute")
    public ResponseEntity<NodeDTO> sumUpAttribute(
            @RequestParam String nodeID,
            @RequestParam String attributeName) {
        val node = graphDBService.sumUpAttribute(nodeID, attributeName);
        return ResponseEntity.ok(nodeDtoMapper.toDto(node));
    }

    /**
     * Exports the graph database to a CSV file
     * @param fileName The name of the CSV file
     * @return The CSV file
     */
    @GetMapping(path = "/csvExport")
    public ResponseEntity<Resource> exportToCSV(
            @RequestParam String fileName) {
        ByteArrayResource resource = graphDBService.generateCSV(fileName);
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.valueOf("application/CSV"))
                .header("Content-Disposition", "attachment; filename=" + fileName + ".csv")
                .body(resource);
    }
}
