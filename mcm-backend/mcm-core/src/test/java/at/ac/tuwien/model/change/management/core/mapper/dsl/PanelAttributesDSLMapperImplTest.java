package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.PanelAttributeDSL;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PanelAttributesDSLMapperImplTest {

    private final PanelAttributesDSLMapperImpl mapper = new PanelAttributesDSLMapperImpl();

    @Test
    void testToDSL() {
        List<PanelAttributeDSL> result = mapper.toDSL(new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")));

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
        List<PanelAttributeDSL> input = List.of(new PanelAttributeDSL("key1", "value1"), new PanelAttributeDSL("key2", "value2"));
        Map<String, String> result = mapper.fromDSL(input);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
    }

    @Test
    void testFromDSL_NullInput() {
        assertNull(mapper.fromDSL(null));
    }

    @Test
    void testFromDSL_EmptyInput() {
        assertNotNull(mapper.fromDSL(List.of()));
    }
}

