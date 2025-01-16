package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.GraphDBService;
import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.dto.QueryDTO;
import at.ac.tuwien.model.change.management.server.mapper.ConfigurationDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.CycleAvoidingMappingContext;
import at.ac.tuwien.model.change.management.server.mapper.NodeDtoMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        var node = graphDBService.loadNode(nodeDtoMapper.fromDto(nodeDTO,context));
        return ResponseEntity.ok(nodeDtoMapper.toDto(node,context));
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
     * @param configurationID the ID of the configuration to load into the database
     * @return the loaded configuration as a data transfer object
     */
    @PostMapping(path = "/configuration/{configurationID}")
    public ResponseEntity<ConfigurationDTO> loadConfiguration(@PathVariable("configurationID") String configurationID) {
        var configuration = graphDBService.loadConfiguration(configurationID);
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
     * Saves the graph database to the repository.
     * @return a response entity with a success message
     */
    @PostMapping(path = "/save")
    public ResponseEntity<String> saveDatabase() {
        val result = graphDBService.saveDBToRepository();

        // Return the result
        if(result)
            return ResponseEntity.ok("Successfully saved the DB to the repository!");
        else
            return ResponseEntity.notFound().build();
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
    @PostMapping(path = "/query")
    public ResponseEntity<String> executeQuery(@RequestBody QueryDTO query) {
        val result = graphDBService.executeQuery(query.query());
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieves all the predecessors of a node even in recursive relation
     * @param nodeID the ID of the node
     * @return a list of predecessor nodes
     */
    @GetMapping(path = "/nodes/predecessors")
    public ResponseEntity<List<NodeDTO>> getPredecessors(
            @RequestParam String nodeID) {
        val nodes = graphDBService.getPredecessors(nodeID);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        return ResponseEntity.ok(nodes.stream().map(it -> nodeDtoMapper.toDto(it, context)).toList());
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
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        return ResponseEntity.ok(nodeDtoMapper.toDto(node, context));
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

    /**
     * Exports a custom query to a CSV file
     * @param fileName The name of the CSV file
     * @param query The custom query which contains subgraph to export
     * @return The CSV file
     */
    @PostMapping(path = "/csvExport")
    public ResponseEntity<Resource> exportQueryToCSV(
            @RequestParam String fileName,
            @RequestBody QueryDTO query
    ) {
        ByteArrayResource resource = graphDBService.generateQueryCSV(fileName, query.query());
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.valueOf("application/CSV"))
                .header("Content-Disposition", "attachment; filename=" + fileName + ".csv")
                .body(resource);
    }

    /**
     * Exports the query made on the graph database to a UXF file
     * @param fileName The name of the UXF file
     * @return The UXF file
     */
    @PostMapping(path = "/queryExport")
    public ResponseEntity<Resource> exportQueryToUXF(
            @RequestParam String fileName,
            @RequestBody QueryDTO query
    ) {
        val uxfFile = graphDBService.generateQueryUXF(query.query());
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/XML"))
                .header("Content-Disposition", "attachment; filename=" + fileName + ".uxf")
                .body(uxfFile);
    }
}
