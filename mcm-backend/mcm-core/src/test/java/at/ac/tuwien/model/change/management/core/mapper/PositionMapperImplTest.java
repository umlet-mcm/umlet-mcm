package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PositionMapperImplTest {

    private final PositionMapperImpl positionMapper = new PositionMapperImpl();

    @Test
    void toGraphProperties_withValidUMLetPosition_returnsCorrectMap() {
        UMLetPosition position = new UMLetPosition();
        position.setX(10);
        position.setY(20);
        position.setWidth(30);
        position.setHeight(40);

        Map<String, Integer> result = positionMapper.toGraphProperties(position);

        assertEquals(10, result.get("x"));
        assertEquals(20, result.get("y"));
        assertEquals(30, result.get("width"));
        assertEquals(40, result.get("height"));
    }

    @Test
    void toGraphProperties_withNullUMLetPosition_returnsEmptyMap() {
        Map<String, Integer> result = positionMapper.toGraphProperties(null);

        assertEquals(0, result.size());
    }

    @Test
    void toPosition_withValidMap_returnsCorrectUMLetPosition() {
        Map<String, Integer> positionMap = Map.of(
                "x", 10,
                "y", 20,
                "width", 30,
                "height", 40
        );

        UMLetPosition result = positionMapper.toLocation(positionMap);

        assertEquals(10, result.getX());
        assertEquals(20, result.getY());
        assertEquals(30, result.getWidth());
        assertEquals(40, result.getHeight());
    }

    @Test
    void toLocation_withNullMap_returnsNull() {
        UMLetPosition result = positionMapper.toLocation(null);

        assertNull(result);
    }

    @Test
    void toLocation_withIncompleteMap_returnsUMLetPositionWithDefaults() {
        Map<String, Integer> positionMap = Map.of(
                "x", 10,
                "y", 20
        );

        UMLetPosition result = positionMapper.toLocation(positionMap);

        assertEquals(10, result.getX());
        assertEquals(20, result.getY());
        assertEquals(0, result.getWidth());
        assertEquals(0, result.getHeight());
    }
}