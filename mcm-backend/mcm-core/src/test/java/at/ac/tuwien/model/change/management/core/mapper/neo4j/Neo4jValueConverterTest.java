package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.value.StringValue;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class Neo4jValueConverterTest {

    @Test
    void convertValueReturnsString() {
        Value value = Values.value("test");
        assertEquals("test", Neo4jValueConverter.convertValue(value));
    }

    @Test
    void convertValueReturnsInteger() {
        Value value = Values.value(123);
        assertEquals(123, Neo4jValueConverter.convertValue(value));
    }

    @Test
    void convertValueReturnsDouble() {
        Value value = Values.value(123.45);
        assertEquals(123.45, Neo4jValueConverter.convertValue(value));
    }

    @Test
    void convertValueReturnsList() {
        Value value = Values.value(Arrays.asList(Values.value("a"), Values.value("b")));
        assertEquals(Arrays.asList("a", "b"), Neo4jValueConverter.convertValue(value));
    }

    @Test
    void convertValueReturnsNullForNullValue() {
        Value value = Values.NULL;
        assertNull(Neo4jValueConverter.convertValue(value));
    }

    @Test
    void convertValueThrowsExceptionForUnsupportedType() {
        Value value = Values.value(true);
        assertThrows(IllegalArgumentException.class, () -> Neo4jValueConverter.convertValue(value));
    }

    @Test
    void convertObjectReturnsStringValue() {
        Object object = "test";
        assertEquals(new StringValue("test"), Neo4jValueConverter.convertObject(object));
    }

    @Test
    void convertObjectReturnsIntegerValue() {
        Object object = 123;
        assertEquals(Values.value(123), Neo4jValueConverter.convertObject(object));
    }

    @Test
    void convertObjectReturnsDoubleValue() {
        Object object = 123.45;
        assertEquals(Values.value(123.45), Neo4jValueConverter.convertObject(object));
    }

    @Test
    void convertObjectReturnsListValue() {
        Object object = Arrays.asList("a", "b");
        assertEquals(Values.value(Arrays.asList(new StringValue("a"), new StringValue("b"))), Neo4jValueConverter.convertObject(object));
    }

    @Test
    void convertObjectReturnsNullValueForNullObject() {
        Object object = null;
        assertEquals(Values.NULL, Neo4jValueConverter.convertObject(object));
    }

    @Test
    void convertObjectThrowsExceptionForUnsupportedObjectType() {
        Object object = new Object();
        assertThrows(IllegalArgumentException.class, () -> Neo4jValueConverter.convertObject(object));
    }
}