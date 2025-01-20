package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.KeyValueDSL;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface KeyValuesDSLMapper {

    List<KeyValueDSL> toObjectDSL(Map<String, Object> properties);

    LinkedHashMap<String, Object> fromObjectDSL(List<KeyValueDSL> propertiesDSL);

    List<KeyValueDSL> toStringDSL(Map<String, String> properties);

    LinkedHashMap<String, String> fromStringDSL(List<KeyValueDSL> propertiesDSL);
}
