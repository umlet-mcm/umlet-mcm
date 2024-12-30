package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.mapper.neo4j.Neo4jTypeAdapter;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.Neo4jValueConverter;
import at.ac.tuwien.model.change.management.graphdb.dao.RawNeo4jService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.value.FloatValue;
import org.neo4j.driver.internal.value.IntegerValue;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.values.storable.DoubleValue;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class GraphDBServiceImplTest {

    @Mock
    private RawNeo4jService rawNeo4jService;

    @InjectMocks
    private GraphDBServiceImpl graphDBService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void executeQuery_returnsJsonString() {
        String query = "MATCH (n) RETURN n";
        List<Map<String, Object>> mockResponse = List.of(Map.of(
                "String", Values.value("value"),
                "Integer", Values.value(1),
                "List", List.of(Values.value("value1"), Values.value("value2")),
                "Double", Values.value(1.0)));
        when(rawNeo4jService.executeRawQuery(query)).thenReturn(mockResponse);

        String result = graphDBService.executeQuery(query);

        List<Map<String, Object>> expectedResponse = List.of(Map.of(
                "String", "value",
                "Integer", 1,
                "List", List.of("value1", "value2"),
                "Double", 1.0));
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Value.class, new Neo4jTypeAdapter())
                .create();
        String expectedJson = gson.toJson(expectedResponse);

        assertEquals(expectedJson, result);
        verify(rawNeo4jService, times(1)).executeRawQuery(query);
    }

    @Test
    void executeQuery_withEmptyResponse_returnsEmptyJsonArray() {
        String query = "MATCH (n) RETURN n";
        List<Map<String, Object>> mockResponse = List.of();
        when(rawNeo4jService.executeRawQuery(query)).thenReturn(mockResponse);

        String result = graphDBService.executeQuery(query);

        assertEquals("[]", result);
        verify(rawNeo4jService, times(1)).executeRawQuery(query);
    }

    @Test
    void generateCSV_returnsByteArrayResource() {
        String fileName = "test.csv";
        ByteArrayResource mockResource = new ByteArrayResource(new byte[0]);
        when(rawNeo4jService.downloadCSV(fileName)).thenReturn(mockResource);

        ByteArrayResource result = graphDBService.generateCSV(fileName);

        assertEquals(mockResource, result);
        verify(rawNeo4jService, times(1)).generateCSV(fileName);
        verify(rawNeo4jService, times(1)).downloadCSV(fileName);
    }

    @Test
    void clearDatabase_invokesRawNeo4jService() {
        graphDBService.clearDatabase();

        verify(rawNeo4jService, times(1)).clearDatabase();
    }
}