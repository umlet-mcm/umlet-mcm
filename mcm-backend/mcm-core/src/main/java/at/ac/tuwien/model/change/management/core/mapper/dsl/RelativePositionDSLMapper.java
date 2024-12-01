package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.dsl.RelativePositionDSL;


public interface RelativePositionDSLMapper {

    RelativePositionDSL toDSL(RelativePosition relativePosition);

    RelativePosition fromDSL(RelativePositionDSL relativePositionDSL);
}
