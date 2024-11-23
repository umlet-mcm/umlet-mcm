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
    private static final String X = "x";
    private static final String Y = "y";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";

    @Override
    public Map<String, Integer> toGraphProperties(UMLetPosition umLetPosition) {
        Map<String, Integer> properties = new HashMap<>();
        if (umLetPosition != null) {
            properties.put(X, umLetPosition.getX());
            properties.put(Y, umLetPosition.getY());
            properties.put(WIDTH, umLetPosition.getWidth());
            properties.put(HEIGHT, umLetPosition.getHeight());
        }
        return properties;
    }

    @Override
    public UMLetPosition toLocation(Map<String, Integer> position) {
        if (position == null) {
            return null;
        }

        var convertedPosition = new UMLetPosition();
        convertedPosition.setX(position.getOrDefault(X,0));
        convertedPosition.setY(position.getOrDefault(Y,0));
        convertedPosition.setWidth(position.getOrDefault(WIDTH,0));
        convertedPosition.setHeight(position.getOrDefault(HEIGHT,0));
        return convertedPosition;
    }
}
