package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.UmletPosition;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UmletPositionUxfMapper {
    UmletPosition toUmletPosition(UmletPositionUxf pos);

    UmletPositionUxf fromUmletPosition(UmletPosition pos);
}
