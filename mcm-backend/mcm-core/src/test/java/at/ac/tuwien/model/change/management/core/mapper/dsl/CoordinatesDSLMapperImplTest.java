package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.dsl.CoordinatesDSL;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CoordinatesDSLMapperImplTest {

    private final CoordinatesDSLMapper coordinatesDSLMapper = new CoordinatesDSLMapperImpl();

    @Test
    void testToDSLWithValidInput() {
        UMLetPosition position = new UMLetPosition(10, 20, 30, 40);
        CoordinatesDSL result = coordinatesDSLMapper.toDSL(position);
        assertNotNull(result);
        assertEquals(10, result.getX());
        assertEquals(20, result.getY());
        assertEquals(30, result.getW());
        assertEquals(40, result.getH());
    }

    @Test
    void testFromDSLWithValidInput() {
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
