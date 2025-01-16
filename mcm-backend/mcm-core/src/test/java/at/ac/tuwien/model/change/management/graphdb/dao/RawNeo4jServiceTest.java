package at.ac.tuwien.model.change.management.graphdb.dao;

import at.ac.tuwien.model.change.management.graphdb.config.Neo4JProperties;
import at.ac.tuwien.model.change.management.graphdb.exceptions.InvalidQueryException;
import lombok.val;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RawNeo4jServiceTest {

    @Mock
    private Driver neo4jDriver;

    @Mock
    private Neo4JProperties properties;

    @Mock
    private Session session;

    @Mock
    private Result result;

    @Mock
    private Record record;

    @InjectMocks
    private RawNeo4jService rawNeo4jService;

    private static final byte[] fileContent = "csv content".getBytes();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(neo4jDriver.session()).thenReturn(session);
    }

    @BeforeAll
    static void createFile() throws IOException {
        val path = Paths.get("exports/fileName.csv");
        Files.createDirectories(path.getParent());
        Files.createFile(path);

        Files.write(path, fileContent);
    }

    @AfterAll
    static void deleteFile() throws IOException {
        Files.delete(Paths.get("exports/fileName.csv"));
        Files.delete(Paths.get("exports"));
    }

    @Test
    void executeRawQueryReturnsResults() {
        String cypherQuery = "MATCH (n) RETURN n";
        when(session.run(cypherQuery)).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.asMap()).thenReturn(Collections.singletonMap("Nodes_Count", 7));

        List<Map<String, Object>> results = rawNeo4jService.executeRawQuery(cypherQuery);

        assertNotNull(results);
        verify(session).run(cypherQuery);
        assertEquals(1, results.size());
        assertEquals(7, results.get(0).get("Nodes_Count"));
    }

    @Test
    void executeRawQueryThrowsInvalidQueryExceptionForInvalidQuery() {
        String cypherQuery = "INVALID QUERY";
        when(session.run(cypherQuery)).thenThrow(new ClientException("Invalid query"));

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.executeRawQuery(cypherQuery));
    }

    @Test
    void generateCSVExecutesExportQuery() {
        String fileName = "fileName";
        when(properties.getRelativeExportsPath()).thenReturn(Paths.get("exports"));

        rawNeo4jService.generateCSV(fileName);

        verify(session).run("CALL apoc.export.csv.all('exports/fileName.csv', {})");
    }

    @Test
    void generateCSVThrowsInvalidQueryExceptionForClientException() {
        String fileName = "fileName";
        when(properties.getRelativeExportsPath()).thenReturn(Paths.get("exports"));
        doThrow(new ClientException("Error Exporting to CSV! No access to DB!")).when(session).run(anyString());

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.generateCSV(fileName));
    }

    @Test
    void generateQueryCSVExecutesExportQuery() {
        String fileName = "fileName";
        String query = "MATCH(n:Node)-[r:RELATION]->(m:Node) RETURN n,r.Name,m.ID";
        when(properties.getRelativeExportsPath()).thenReturn(Paths.get("exports"));

        rawNeo4jService.generateQueryCSV(fileName, query);

        verify(session).run("WITH \"MATCH(n:Node)-[r:RELATION]->(m:Node) RETURN n,r.Name,m.ID\" AS query\n" +
                "CALL apoc.export.csv.query(query, \"exports/fileName.csv\", {})\n" +
                "YIELD file, source, format, nodes, relationships, properties, time, rows, batchSize, batches, done, data\n" +
                "RETURN file, source, format, nodes, relationships, properties, time, rows, batchSize, batches, done, data;");
    }

    @Test
    void generateQueryCSVThrowsInvalidQueryExceptionForClientException() {
        String fileName = "fileName";
        String query = "MATCH(n:Node)-[r:RELATION]->(m:Node) RETURN n,r.Name,m.ID";
        when(properties.getRelativeExportsPath()).thenReturn(Paths.get("exports"));
        doThrow(new ClientException("Error Exporting to CSV! No access to DB!")).when(session).run(anyString());

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.generateQueryCSV(fileName,query));
    }

    @Test
    void downloadCSVReturnsByteArrayResource() throws IOException {
        String fileName = "fileName";

        when(properties.getExportsPath()).thenReturn(Paths.get("exports"));

        ByteArrayResource resource = rawNeo4jService.downloadCSV(fileName);

        assertNotNull(resource);
        assertArrayEquals(fileContent, resource.getByteArray());
    }

    @Test
    void downloadCSVThrowsInvalidQueryExceptionForIOException() throws IOException {
        String fileName = "nonExistingFile";
        when(properties.getExportsPath()).thenReturn(Paths.get("exports"));

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.downloadCSV(fileName));
    }

    @Test
    void clearDatabaseExecutesDeleteQuery() {
        rawNeo4jService.clearDatabase();

        verify(session).run("MATCH (n) DETACH DELETE n");
    }

    @Test
    void clearDatabaseThrowsInvalidQueryExceptionForClientException() {
        doThrow(new ClientException("Error clearing database!")).when(session).run(anyString());

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.clearDatabase());
    }

    @Test
    void getOnlyQuerySubgraphKeepsSpecifiedNodes() {
        String query = "MATCH (n:Node) RETURN n";
        Value value = mock(Value.class, RETURNS_DEEP_STUBS);
        when(session.run(query)).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.size()).thenReturn(1);
        when(record.get(0)).thenReturn(value);
        when(record.get(0).type().name()).thenReturn("NODE");
        when(record.get(0).asEntity().elementId()).thenReturn("123");

        rawNeo4jService.getOnlyQuerySubgraph(query);

        verify(session).run("MATCH (n) WHERE NOT elementId(n) IN $idsToKeep DETACH DELETE n", Map.of("idsToKeep", List.of("123")));
    }

    @Test
    void getOnlyQuerySubgraphWithUUIDKeepsSpecifiedNodes() {
        String query = "MATCH (n:Node) RETURN n";
        Value value = mock(Value.class, RETURNS_DEEP_STUBS);
        val uuid = UUID.randomUUID().toString();
        when(session.run(query)).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.size()).thenReturn(1);
        when(record.get(0)).thenReturn(value);
        when(record.get(0).type().name()).thenReturn("STRING");
        when(record.get(0).asString()).thenReturn("4:" + uuid + ":11");

        rawNeo4jService.getOnlyQuerySubgraph(query);

        verify(session).run("MATCH (n) WHERE NOT elementId(n) IN $idsToKeep DETACH DELETE n", Map.of("idsToKeep", List.of("4:" + uuid + ":11")));
    }

    @Test
    void getOnlyQuerySubgraphThrowsInvalidQueryExceptionForEmptyResult() {
        String query = "MATCH (n:Node) RETURN n";
        when(session.run(query)).thenReturn(result);
        when(result.hasNext()).thenReturn(false);

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.getOnlyQuerySubgraph(query));
    }

    @Test
    void getOnlyQuerySubgraphThrowsInvalidQueryExceptionForInvalidColumnType() {
        String query = "MATCH (n:Node) RETURN n";
        Value value = mock(Value.class, RETURNS_DEEP_STUBS);

        when(session.run(query)).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.size()).thenReturn(1);
        when(record.get(0)).thenReturn(value);
        when(record.get(0).type().name()).thenReturn("INTEGER");

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.getOnlyQuerySubgraph(query));
    }

    @Test
    void getOnlyQuerySubgraphThrowsInvalidQueryExceptionForInvalidUUIDString() {
        String query = "MATCH (n:Node) RETURN 'invalid-uuid'";
        Value value = mock(Value.class, RETURNS_DEEP_STUBS);

        when(session.run(query)).thenReturn(result);
        when(result.hasNext()).thenReturn(true, false);
        when(result.next()).thenReturn(record);
        when(record.size()).thenReturn(1);
        when(record.get(0)).thenReturn(value);
        when(record.get(0).type().name()).thenReturn("STRING");
        when(record.get(0).asString()).thenReturn("4:invalid-uuid:11");

        assertThrows(InvalidQueryException.class, () -> rawNeo4jService.getOnlyQuerySubgraph(query));
    }

}