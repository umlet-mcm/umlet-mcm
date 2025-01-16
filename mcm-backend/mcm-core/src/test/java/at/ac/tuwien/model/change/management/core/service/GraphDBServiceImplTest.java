package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.ConfigurationEntityMapper;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.Neo4jTypeAdapter;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.Neo4jValueConverter;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.NodeEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.dao.ConfigurationEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.NodeEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.RawNeo4jService;
import at.ac.tuwien.model.change.management.graphdb.entities.ConfigurationEntity;
import at.ac.tuwien.model.change.management.graphdb.exceptions.InvalidQueryException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.val;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.ReflectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class GraphDBServiceImplTest {

    @Mock
    private RawNeo4jService rawNeo4jService;

    @Mock
    private UxfService uxfService;

    @Mock
    private ConfigurationEntityDAO configurationEntityDAO;

    @Mock
    private NodeEntityDAO nodeEntityDAO;

    @Mock
    private ConfigurationEntityMapper configurationEntityMapper;

    @Mock
    private NodeEntityMapper nodeEntityMapper;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private GraphDBServiceImpl graphDBService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionUtils.setField(ReflectionUtils.findRequiredField(GraphDBServiceImpl.class, "configurationService"), graphDBService, configurationService);
        ReflectionUtils.setField(ReflectionUtils.findRequiredField(GraphDBServiceImpl.class, "uxfService"), graphDBService, uxfService);
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

    @Test
    void generateQueryUXF_returnsByteArrayResource() throws UxfException {
        String query = "MATCH (n) RETURN n";
        String uxfFile = "<uxf content>";
        ByteArrayResource expectedResource = new ByteArrayResource(uxfFile.getBytes(StandardCharsets.UTF_8));

        doNothing().when(rawNeo4jService).getOnlyQuerySubgraph(query);
        when(configurationEntityMapper.fromEntity(any())).thenReturn(new Configuration());
        when(configurationEntityMapper.toEntity(any())).thenReturn(new ConfigurationEntity());
        when(configurationEntityDAO.findAll()).thenReturn(List.of(new ConfigurationEntity()));
        when(configurationService.getConfigurationByName(any())).thenReturn(new Configuration());
        when(uxfService.exportModel(any(Model.class))).thenReturn(uxfFile);
        when(nodeEntityDAO.findAll()).thenReturn(new ArrayList<>());
        when(nodeEntityMapper.fromEntity(any())).thenReturn(new Node());

        ByteArrayResource result = graphDBService.generateQueryUXF(query);

        assertEquals(expectedResource, result);
        verify(rawNeo4jService, times(1)).getOnlyQuerySubgraph(query);
        verify(uxfService, times(1)).exportModel(any(Model.class));
        verify(rawNeo4jService, times(1)).clearDatabase();
        verify(configurationEntityDAO, times(1)).save(any(ConfigurationEntity.class));
    }

    @Test
    void generateQueryUXF_throwsInvalidQueryException() {
        String query = "MATCH (n) RETURN n";

        doThrow(new InvalidQueryException("Invalid query")).when(rawNeo4jService).getOnlyQuerySubgraph(query);
        when(configurationEntityMapper.fromEntity(any())).thenReturn(new Configuration());
        when(configurationEntityMapper.toEntity(any())).thenReturn(new ConfigurationEntity());
        when(configurationEntityDAO.findAll()).thenReturn(List.of(new ConfigurationEntity()));

        InvalidQueryException exception = assertThrows(InvalidQueryException.class, () -> graphDBService.generateQueryUXF(query));
        assertEquals("Invalid query", exception.getMessage());
        verify(rawNeo4jService, times(1)).getOnlyQuerySubgraph(query);
        verify(rawNeo4jService, times(1)).clearDatabase();
        verify(configurationEntityDAO, times(1)).save(any(ConfigurationEntity.class));
    }
}