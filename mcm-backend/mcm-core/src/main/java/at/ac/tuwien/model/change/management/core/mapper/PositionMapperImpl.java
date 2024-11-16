package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * The currently used implementation of the PositionMapper
 */
@Component
public class PositionMapperImpl implements PositionMapper {
    @Override
    public Map<String, Integer> toGraphProperties(UMLetPosition umLetPosition) {
        Map<String, Integer> properties = new HashMap<>();
        if (umLetPosition != null) {
            properties.put("x", umLetPosition.x());
            properties.put("y", umLetPosition.y());
            properties.put("width", umLetPosition.width());
            properties.put("height", umLetPosition.height());
        }
        return properties;
    }

    @Override
    public UMLetPosition toLocation(Map<String, ?> properties) {
        if (properties == null) {
            return null;
        }
        return new UMLetPosition((Integer) properties.get("x"), (Integer) properties.get("y"), (Integer) properties.get("width"), (Integer) properties.get("height"));
    }
}
