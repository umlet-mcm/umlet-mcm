package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.dsl.PropertyDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

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
                .filter(entry -> !entry.getKey().equals(AttributeKeys.TAGS)) // tags have their own field
                .map(entry -> new PropertyDSL(entry.getKey(), entry.getValue().toString()))
                .collect(Collectors.toList());

    }

    @Override
    public Map<String, Object> fromDSL(List<PropertyDSL> propertiesDSL) {
        if (propertiesDSL == null) return null;

        return propertiesDSL.stream().collect(Collectors.toMap(PropertyDSL::getKey, PropertyDSL::getValue));
    }
}
