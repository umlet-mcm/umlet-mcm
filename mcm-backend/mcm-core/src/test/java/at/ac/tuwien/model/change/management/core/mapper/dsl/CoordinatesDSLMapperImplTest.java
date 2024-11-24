package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.CoordinatesDSL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CoordinatesDSLMapperImpl.class)
public class CoordinatesDSLMapperImplTest {

    @Autowired
    private CoordinatesDSLMapper coordinatesDSLMapper;

    @Test
    void testToDSLWithValidInput() throws DSLException {
        UMLetPosition position = new UMLetPosition();
        position.setX(10);
        position.setY(20);
        position.setWidth(30);
        position.setHeight(40);
        CoordinatesDSL result = coordinatesDSLMapper.toDSL(position);
        assertNotNull(result);
        assertEquals(10, result.getX());
        assertEquals(20, result.getY());
        assertEquals(30, result.getW());
        assertEquals(40, result.getH());
    }

    @Test
    void testToDSLWithNullInputThrowsException() throws DSLException {
        assertThrows(DSLException.class, () -> coordinatesDSLMapper.toDSL(null));
    }

    @Test
    void testFromDSLWithValidInput() throws DSLException {
        CoordinatesDSL coordinates = new CoordinatesDSL();
        coordinates.setX(10);
        coordinates.setY(20);
        coordinates.setW(30);
        coordinates.setH(40);
        UMLetPosition result = coordinatesDSLMapper.fromDSL(coordinates);
        assertNotNull(result);
        assertEquals(10, result.getX());
        assertEquals(20, result.getY());
        assertEquals(30, result.getWidth());
        assertEquals(40, result.getHeight());
    }

}
