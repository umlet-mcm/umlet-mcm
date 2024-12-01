package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.CoordinatesDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CoordinatesDSLMapperImpl implements CoordinatesDSLMapper {
    @Override
    public CoordinatesDSL toDSL(UMLetPosition umLetPosition) {
        if (umLetPosition == null) return null;
        return new CoordinatesDSL(umLetPosition.getX(), umLetPosition.getY(), umLetPosition.getWidth(), umLetPosition.getHeight());
    }

    @Override
    public UMLetPosition fromDSL(CoordinatesDSL coordinatesDSL) {
        if (coordinatesDSL == null) return null;
        return new UMLetPosition(coordinatesDSL.getX(), coordinatesDSL.getY(), coordinatesDSL.getW(), coordinatesDSL.getH());
    }
}
