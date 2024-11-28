package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.dsl.RelativePositionDSL;


public interface RelativePositionDSLMapper {

    RelativePositionDSL toDSL(RelativePosition relativePosition) throws DSLException;

    RelativePosition fromDSL(RelativePositionDSL relativePositionDSL) throws DSLException;
}
