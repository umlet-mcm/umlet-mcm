package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = MapperTestConfig.class)
public class UmletPositionUxfMapperTest {

    private UmletPositionUxfMapper umletPositionUxfMapper = Mappers.getMapper(UmletPositionUxfMapper.class);

    @Test
    public void testFromUmletPosition() {
        UMLetPosition umletPosition = new UMLetPosition(1, 2, 3, 4);

        UmletPositionUxf umletPositionUxf = umletPositionUxfMapper.fromUmletPosition(umletPosition, 10);

        assertEquals(umletPosition.getX(), umletPositionUxf.getX());
        assertEquals(umletPosition.getY(), umletPositionUxf.getY());
        assertEquals(umletPosition.getWidth(), umletPositionUxf.getWidth());
        assertEquals(umletPosition.getHeight(), umletPositionUxf.getHeight());
    }

    @Test
    public void testToUmletPosition() {
        UmletPositionUxf umletPositionUxf = new UmletPositionUxf();
        umletPositionUxf.setX(1);
        umletPositionUxf.setY(2);
        umletPositionUxf.setWidth(3);
        umletPositionUxf.setHeight(4);

        UMLetPosition umletPosition = umletPositionUxfMapper.toUmletPosition(umletPositionUxf, 10);

        assertEquals(umletPosition.getX(), umletPositionUxf.getX());
        assertEquals(umletPosition.getY(), umletPositionUxf.getY());
        assertEquals(umletPosition.getWidth(), umletPositionUxf.getWidth());
        assertEquals(umletPosition.getHeight(), umletPositionUxf.getHeight());
    }
}
