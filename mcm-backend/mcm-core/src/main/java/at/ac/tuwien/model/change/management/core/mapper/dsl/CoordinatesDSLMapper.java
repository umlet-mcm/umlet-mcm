package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.CoordinatesDSL;

public interface CoordinatesDSLMapper {
    CoordinatesDSL toDSL(UMLetPosition umLetPosition) throws DSLException;

    UMLetPosition fromDSL(CoordinatesDSL coordinatesDSL) throws DSLException;
}
