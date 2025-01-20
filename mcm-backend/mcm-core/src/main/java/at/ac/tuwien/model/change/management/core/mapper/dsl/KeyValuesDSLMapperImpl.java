package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.KeyValueDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class KeyValuesDSLMapperImpl implements KeyValuesDSLMapper {
    @Override
    public List<KeyValueDSL> toObjectDSL(Map<String, Object> keyValues) {
        if (keyValues == null) return null;

        return keyValues.entrySet().stream()
                .map(entry -> new KeyValueDSL(entry.getKey(), entry.getValue().toString()))
                .collect(Collectors.toList());

    }

    @Override
    public LinkedHashMap<String, Object> fromObjectDSL(List<KeyValueDSL> keyValuesDSL) {
        if (keyValuesDSL == null) return null;

        return keyValuesDSL.stream()
                .collect(Collectors.toMap(
                        KeyValueDSL::getKey,
                        propertyDSL -> parseValue(propertyDSL.getValue()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    @Override
    public List<KeyValueDSL> toStringDSL(Map<String, String> keyValues) {
        if (keyValues == null) return null;

        return keyValues.entrySet().stream()
                .map(entry -> new KeyValueDSL(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public LinkedHashMap<String, String> fromStringDSL(List<KeyValueDSL> keyValuesDSL) {
        if (keyValuesDSL == null) return null;

        return keyValuesDSL.stream()
                .collect(Collectors.toMap(
                        KeyValueDSL::getKey,
                        KeyValueDSL::getValue,
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
