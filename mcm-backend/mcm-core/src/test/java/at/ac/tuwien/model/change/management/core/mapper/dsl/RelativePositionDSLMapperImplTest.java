package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.dsl.RelativePositionDSL;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RelativePositionDSLMapperImplTest {

    private final RelativePositionDSLMapperImpl mapper = new RelativePositionDSLMapperImpl();

    @Test
    void testToDSL() {
        RelativePosition relativePosition = new RelativePosition(10, 20, 5, 15);

        RelativePositionDSL result = mapper.toDSL(relativePosition);

        assertNotNull(result);
        assertEquals(relativePosition.getAbsX(), result.getAbsX());
        assertEquals(relativePosition.getAbsY(), result.getAbsY());
        assertEquals(relativePosition.getOffsetX(), result.getOffsetX());
        assertEquals(relativePosition.getOffsetY(), result.getOffsetY());
    }

    @Test
    void testFromDSL() {
        RelativePositionDSL relativePositionDSL = new RelativePositionDSL(30, 40, 10, 25);

        RelativePosition result = mapper.fromDSL(relativePositionDSL);

        assertNotNull(result);
        assertEquals(relativePositionDSL.getAbsX(), result.getAbsX());
        assertEquals(relativePositionDSL.getAbsY(), result.getAbsY());
        assertEquals(relativePositionDSL.getOffsetX(), result.getOffsetX());
        assertEquals(relativePositionDSL.getOffsetY(), result.getOffsetY());
    }
}
