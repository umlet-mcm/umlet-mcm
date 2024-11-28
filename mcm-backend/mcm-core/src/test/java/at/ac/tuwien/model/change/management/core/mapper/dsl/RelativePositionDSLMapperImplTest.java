package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.dsl.RelativePositionDSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        RelativePositionDSLMapperImpl.class
})
class RelativePositionDSLMapperImplTest {

    @Autowired
    private RelativePositionDSLMapperImpl mapper;

    @Test
    void testToDSL() throws DSLException {
        RelativePosition relativePosition = new RelativePosition(10, 20, 5, 15);

        RelativePositionDSL result = mapper.toDSL(relativePosition);

        assertNotNull(result);
        assertEquals(relativePosition.getAbsX(), result.getAbsX());
        assertEquals(relativePosition.getAbsY(), result.getAbsY());
        assertEquals(relativePosition.getOffsetX(), result.getOffsetX());
        assertEquals(relativePosition.getOffsetY(), result.getOffsetY());
    }

    @Test
    void testToDSL_NullInput() throws DSLException {
        assertThrows(DSLException.class, () -> mapper.toDSL(null));
    }

    @Test
    void testFromDSL() throws DSLException {
        RelativePositionDSL relativePositionDSL = new RelativePositionDSL(30, 40, 10, 25);

        RelativePosition result = mapper.fromDSL(relativePositionDSL);

        assertNotNull(result);
        assertEquals(relativePositionDSL.getAbsX(), result.getAbsX());
        assertEquals(relativePositionDSL.getAbsY(), result.getAbsY());
        assertEquals(relativePositionDSL.getOffsetX(), result.getOffsetX());
        assertEquals(relativePositionDSL.getOffsetY(), result.getOffsetY());
    }

    @Test
    void testFromDSL_NullInputThrowsException() throws DSLException {
        assertThrows(DSLException.class, () -> mapper.fromDSL(null));
    }
}
