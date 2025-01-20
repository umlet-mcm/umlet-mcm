package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.KeyValueDSL;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KeyValuesDSLMapperImplTest {

    private final KeyValuesDSLMapperImpl mapper = new KeyValuesDSLMapperImpl();

    @Test
    void testToObjectDSL() {
        Map<String, Object> input = Map.of("key1", "value1", "key2", "value2");
        List<KeyValueDSL> result = mapper.toObjectDSL(input);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getKey().equals("key1")));
    }

    @Test
    void testToObjectDSL_NullInput() {
        assertNull(mapper.toObjectDSL(null));
    }

    @Test
    void testFromObjectDSL() {
        List<KeyValueDSL> input = List.of(new KeyValueDSL("key1", "value1"), new KeyValueDSL("key2", "value2"));
        Map<String, Object> result = mapper.fromObjectDSL(input);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
    }

    @Test
    void testFromObjectDSL_IntValue() {
        List<KeyValueDSL> input = List.of(new KeyValueDSL("key1", "15"));
        Map<String, Object> result = mapper.fromObjectDSL(input);

        assertNotNull(result);
        assertInstanceOf(Integer.class, result.get("key1"));
    }

    @Test
    void testFromObjectDSL_FloatValue() {
        List<KeyValueDSL> input = List.of(new KeyValueDSL("key1", "15.5"));
        Map<String, Object> result = mapper.fromObjectDSL(input);

        assertNotNull(result);
        assertInstanceOf(Float.class, result.get("key1"));
    }
}
