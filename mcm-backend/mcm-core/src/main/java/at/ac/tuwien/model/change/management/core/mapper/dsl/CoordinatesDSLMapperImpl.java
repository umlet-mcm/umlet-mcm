package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.CoordinatesDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CoordinatesDSLMapperImpl implements CoordinatesDSLMapper {
    @Override
    public CoordinatesDSL toDSL(UMLetPosition umLetPosition) throws DSLException {
        if (umLetPosition == null) {
            throw new DSLException("Coordinates of the element cannot be null");
        }

        CoordinatesDSL coordinatesDSL = new CoordinatesDSL();
        coordinatesDSL.setX(umLetPosition.getX());
        coordinatesDSL.setY(umLetPosition.getY());
        coordinatesDSL.setW(umLetPosition.getWidth());
        coordinatesDSL.setH(umLetPosition.getHeight());

        return coordinatesDSL;
    }

    @Override
    public UMLetPosition fromDSL(CoordinatesDSL coordinatesDSL) throws DSLException {
        if (coordinatesDSL == null) {
            throw new DSLException("Coordinates of the element cannot be null");
        }

        UMLetPosition umLetPosition = new UMLetPosition();
        umLetPosition.setX(coordinatesDSL.getX());
        umLetPosition.setY(coordinatesDSL.getY());
        umLetPosition.setWidth(coordinatesDSL.getW());
        umLetPosition.setHeight(coordinatesDSL.getH());

        return umLetPosition;
    }
}
