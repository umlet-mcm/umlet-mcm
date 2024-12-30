package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.neo4j.driver.Value;

import java.lang.reflect.Type;

/**
 * Type adapter for serializing Neo4j values to JSON.
 */
public class Neo4jTypeAdapter implements JsonSerializer<Value> {


    @Override
    public JsonElement serialize(Value src, Type typeOfSrc, JsonSerializationContext context) {
        // Use the Neo4jValueConverter to convert the Value to a suitable type for JSON serialization
        Object convertedValue = Neo4jValueConverter.convertValue(src);
        return context.serialize(convertedValue);
    }
}
