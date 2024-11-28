package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.PropertyDSL;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PropertiesDSLMapperImplTest {

    private final PropertiesDSLMapperImpl mapper = new PropertiesDSLMapperImpl();

    @Test
    void testToDSL() {
        Map<String, Object> input = Map.of("key1", "value1", "key2", "value2");
        List<PropertyDSL> result = mapper.toDSL(input);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getKey().equals("key1")));
    }

    @Test
    void testToDSL_NullInput() {
        assertNull(mapper.toDSL(null));
    }

    @Test
    void testFromDSL() {
        List<PropertyDSL> input = List.of(new PropertyDSL("key1", "value1"), new PropertyDSL("key2", "value2"));
        Map<String, Object> result = mapper.fromDSL(input);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
    }
}
