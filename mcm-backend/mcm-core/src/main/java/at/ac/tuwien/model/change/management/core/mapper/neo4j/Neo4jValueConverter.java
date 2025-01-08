package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.types.TypeSystem;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting between Neo4j values and Java objects.
 */
public class Neo4jValueConverter {

    /**
     * Converts a Neo4j value to a Java object.
     * @param value the Neo4j value class
     * @return the Java object
     */
    public static Object convertValue(Value value) {
        if (value == null || value.isNull()) {
            return null;
        }

        if (value.hasType(TypeSystem.getDefault().STRING())) {
            return value.asString();
        } else if (value.hasType(TypeSystem.getDefault().INTEGER())) {
            return value.asInt();
        } else if (value.hasType(TypeSystem.getDefault().FLOAT())) {
            return value.asDouble();
        } else if (value.hasType(TypeSystem.getDefault().LIST())) {
            List<Object> listValues = value.asList(Neo4jValueConverter::convertValue);
            return listValues;
        }

        throw new IllegalArgumentException("Unsupported value type: " + value.type().name());
    }

    /**
     * Converts a Java object to a Neo4j value.
     * @param object the Java object
     * @return the Neo4j value
     */
    public static Value convertObject(Object object) {
        if (object == null) {
            return Values.NULL;
        }

        if (object instanceof String) {
            return new StringValue((String) object);
        } else if (object instanceof Integer) {
            return Values.value((Integer) object);
        } else if (object instanceof Double) {
            return Values.value((Double) object);
        } else if (object instanceof Float) {
            return Values.value((Float) object);
        } else if (object instanceof List) {
            List<Value> listValues = ((List<?>) object).stream()
                    .map(Neo4jValueConverter::convertObject)
                    .collect(Collectors.toList());
            return Values.value(listValues);
        }

        throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
    }
}