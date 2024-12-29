package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The mapper will normalize and denormalize the coordinates based on the zoom
 * level
 */
@Mapper
public interface UmletPositionUxfMapper {

    @Mapping(target = "x", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.normalizeCoordinate(pos.getX(), zoomLevel))")
    @Mapping(target = "y", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.normalizeCoordinate(pos.getY(), zoomLevel))")
    @Mapping(target = "width", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.normalizeCoordinate(pos.getWidth(), zoomLevel))")
    @Mapping(target = "height", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.normalizeCoordinate(pos.getHeight(), zoomLevel))")
    UMLetPosition toUmletPosition(UmletPositionUxf pos, @Context int zoomLevel);

    @Mapping(target = "x", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.denormalizeCoordinate(pos.getX(), zoomLevel))")
    @Mapping(target = "y", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.denormalizeCoordinate(pos.getY(), zoomLevel))")
    @Mapping(target = "width", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.denormalizeCoordinate(pos.getWidth(), zoomLevel))")
    @Mapping(target = "height", expression = "java(at.ac.tuwien.model.change.management.core.model.utils.ParserUtils.denormalizeCoordinate(pos.getHeight(), zoomLevel))")
    UmletPositionUxf fromUmletPosition(UMLetPosition pos, @Context int zoomLevel);
}
