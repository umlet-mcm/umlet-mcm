package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.dsl.RelativePositionDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RelativePositionDSLMapperImpl implements RelativePositionDSLMapper {
    @Override
    public RelativePositionDSL toDSL(RelativePosition relativePosition) throws DSLException {
        if (relativePosition == null) {
            throw new DSLException("Relative position cannot be null");
        }
        return new RelativePositionDSL(relativePosition.getAbsX(), relativePosition.getAbsY(), relativePosition.getOffsetX(), relativePosition.getOffsetY());
    }

    @Override
    public RelativePosition fromDSL(RelativePositionDSL relativePositionDSL) throws DSLException {
        if (relativePositionDSL == null) {
            throw new DSLException("Relative position cannot be null");
        }

        // Remove the offset from the absolute position as the constructor of RelativePosition do some calculations
        // TODO: consider having a constructor that takes the absolute positions instead of refX and refY
        return new RelativePosition(
                relativePositionDSL.getOffsetX(),
                relativePositionDSL.getOffsetY(),
                relativePositionDSL.getAbsX() - relativePositionDSL.getOffsetX(),
                relativePositionDSL.getAbsY() - relativePositionDSL.getOffsetY()
        );
    }
}
