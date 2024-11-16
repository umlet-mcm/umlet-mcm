package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;

import java.util.Map;

/**
 * Mapper for converting between UMLetPosition and Node Property Position
 */
public interface PositionMapper {
    /**
     * Converts a UMLetPosition to a Map of properties
     *
     * @param umLetPosition the UMLetPosition to convert
     * @return the converted Map of properties
     */
    Map<String, Integer> toGraphProperties(UMLetPosition umLetPosition);

    /**
     * Converts a Map of properties to a UMLetPosition
     *
     * @param properties the Map of properties to convert
     * @return the converted UMLetPosition
     */
    UMLetPosition toLocation(Map<String, ?> properties);
}
