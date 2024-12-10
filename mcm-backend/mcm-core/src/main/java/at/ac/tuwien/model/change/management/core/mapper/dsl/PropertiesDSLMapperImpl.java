package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.PropertyDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PropertiesDSLMapperImpl implements PropertiesDSLMapper {
    @Override
    public List<PropertyDSL> toDSL(Map<String, Object> properties) {
        if (properties == null) return null;

        return properties.entrySet().stream()
                .map(entry -> new PropertyDSL(entry.getKey(), entry.getValue().toString()))
                .collect(Collectors.toList());

    }

    @Override
    public LinkedHashMap<String, Object> fromDSL(List<PropertyDSL> propertiesDSL) {
        if (propertiesDSL == null) return null;

        return propertiesDSL.stream()
                .collect(Collectors.toMap(
                        PropertyDSL::getKey,
                        propertyDSL -> parseValue(propertyDSL.getValue()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    private Object parseValue(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e1) {
            try {
                return Float.valueOf(value);
            } catch (NumberFormatException e2) {
                return value;
            }
        }
    }
}
