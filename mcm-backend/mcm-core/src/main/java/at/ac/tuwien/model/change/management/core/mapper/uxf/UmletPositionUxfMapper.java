package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UmletPositionUxfMapper {
    int DEFAULT_ZOOM_LEVEL = 10;

    @Mapping(target = "x", expression = "java(normalizeCoordinate(pos.getX(), zoomLevel))")
    @Mapping(target = "y", expression = "java(normalizeCoordinate(pos.getY(), zoomLevel))")
    @Mapping(target = "width", expression = "java(normalizeCoordinate(pos.getWidth(), zoomLevel))")
    @Mapping(target = "height", expression = "java(normalizeCoordinate(pos.getHeight(), zoomLevel))")
    UMLetPosition toUmletPosition(UmletPositionUxf pos, @Context int zoomLevel);

    @Mapping(target = "x", expression = "java(denormalizeCoordinate(pos.getX(), zoomLevel))")
    @Mapping(target = "y", expression = "java(denormalizeCoordinate(pos.getY(), zoomLevel))")
    @Mapping(target = "width", expression = "java(denormalizeCoordinate(pos.getWidth(), zoomLevel))")
    @Mapping(target = "height", expression = "java(denormalizeCoordinate(pos.getHeight(), zoomLevel))")
    UmletPositionUxf fromUmletPosition(UMLetPosition pos, @Context int zoomLevel);

    default int normalizeCoordinate(int val, int zoomLevel) {
        return Math.round(val * (zoomLevel / (float) DEFAULT_ZOOM_LEVEL));
    }

    default int denormalizeCoordinate(int val, int zoomLevel) {
        return Math.round(val * (DEFAULT_ZOOM_LEVEL / (float) zoomLevel));
    }
}
