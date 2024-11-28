package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.PropertyDSL;

import java.util.List;
import java.util.Map;

public interface PropertiesDSLMapper {

    List<PropertyDSL> toDSL(Map<String, Object> properties);

    Map<String, Object> fromDSL(List<PropertyDSL> propertiesDSL);
}
