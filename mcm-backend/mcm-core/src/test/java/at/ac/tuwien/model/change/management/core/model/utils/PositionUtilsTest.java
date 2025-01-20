package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PositionUtilsTest {

    @Test
    public void testOverlap() {
        UmletPositionUxf p1 = new UmletPositionUxf(10, 10, 50, 50);
        UmletPositionUxf p2 = new UmletPositionUxf(20, 10, 50, 50);
        UmletPositionUxf p3 = new UmletPositionUxf(5, 5, 2, 2);
        UmletPositionUxf p4 = new UmletPositionUxf(0, 10, 10, 10);

        assertTrue(PositionUtils.overlap(p1, p2));
        assertFalse(PositionUtils.overlap(p1, p3));
        assertFalse(PositionUtils.overlap(p1, p4));
    }

    @Test
    public void testGetBoundingBox() {
        ModelUxf m = new ModelUxf();
        ElementUxf e1 = new ElementUxf();
        e1.setUmletPosition(new UmletPositionUxf(10, 10, 50, 50));
        m.setElements(Set.of(e1));

        assertEquals(new UmletPositionUxf(10, 10, 50, 50), PositionUtils.getBoundingBox(List.of(m)));

        ElementUxf e2 = new ElementUxf();
        e2.setUmletPosition(new UmletPositionUxf(20, 20, 100, 100));
        m.setElements(Set.of(e1, e2));

        assertEquals(new UmletPositionUxf(10, 10, 110, 110), PositionUtils.getBoundingBox(List.of(m)));
    }

    @Test
    public void testTranslatePosition() {
        UmletPositionUxf p = new UmletPositionUxf(10, 10, 50, 50);
        PositionUtils.translatePosition(p, 10, 10);
        assertEquals(new UmletPositionUxf(20, 20, 50, 50), p);
        PositionUtils.translatePosition(p, -5, -10);
        assertEquals(new UmletPositionUxf(15, 10, 50, 50), p);
    }

    @Test
    public void testTranslateModel() {
        ModelUxf m = new ModelUxf();
        ElementUxf e1 = new ElementUxf();
        e1.setUmletPosition(new UmletPositionUxf(10, 10, 50, 50));
        m.setElements(Set.of(e1));
        PositionUtils.translateModel(m, 10, 10);
        assertEquals(new UmletPositionUxf(20, 20, 50, 50),
                m.getElements().stream().findFirst().orElseThrow().getUmletPosition());

        PositionUtils.translateModel(m, -10, 10);
        assertEquals(new UmletPositionUxf(10, 30, 50, 50),
                m.getElements().stream().findFirst().orElseThrow().getUmletPosition());
    }

    @Test
    public void testLayOutModelsLeftToRight() {
        ModelUxf m1 = new ModelUxf();
        ElementUxf e1 = new ElementUxf();
        e1.setUmletPosition(new UmletPositionUxf(10, 10, 50, 50));
        m1.setElements(Set.of(e1));

        ModelUxf m2 = new ModelUxf();
        ElementUxf e2 = new ElementUxf();
        e2.setUmletPosition(new UmletPositionUxf(20, 20, 50, 50));
        m2.setElements(Set.of(e2));

        ArrayList<Pair<ModelUxf, UmletPositionUxf>> modelsWithBB = new ArrayList<>();
        modelsWithBB.add(new MutablePair<>(m1, PositionUtils.getBoundingBox(List.of(m1))));
        modelsWithBB.add(new MutablePair<>(m2, PositionUtils.getBoundingBox(List.of(m2))));

        PositionUtils.layOutModelsLeftToRight(modelsWithBB, 0, 50);

        assertEquals(0, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(50 / 2, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());

        assertEquals(50 + PositionUtils.MODEL_PADDING, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(50 / 2, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());
    }

    @Test
    public void testAlignModelsNoChange() {
        ModelUxf m1 = new ModelUxf();
        ElementUxf e1 = new ElementUxf();
        e1.setUmletPosition(new UmletPositionUxf(10, 10, 50, 50));
        m1.setElements(Set.of(e1));

        ModelUxf m2 = new ModelUxf();
        ElementUxf e2 = new ElementUxf();
        e2.setUmletPosition(new UmletPositionUxf(100, 200, 50, 50));
        m2.setElements(Set.of(e2));

        PositionUtils.alignModels(List.of(m1, m2));

        assertEquals(10, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(10, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());

        assertEquals(100, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(200, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());
    }

    @Test
    public void testAlignModelsSomeOverlap() {
        ModelUxf m1 = new ModelUxf();
        ElementUxf e1 = new ElementUxf();
        e1.setUmletPosition(new UmletPositionUxf(10, 10, 50, 50));
        m1.setElements(Set.of(e1));

        ModelUxf m2 = new ModelUxf();
        ElementUxf e2 = new ElementUxf();
        e2.setUmletPosition(new UmletPositionUxf(110, 210, 20, 20));
        m2.setElements(Set.of(e2));

        ModelUxf m3 = new ModelUxf();
        ElementUxf e3 = new ElementUxf();
        e3.setUmletPosition(new UmletPositionUxf(100, 200, 50, 50));
        m3.setElements(Set.of(e3));

        PositionUtils.alignModels(List.of(m1, m2, m3));

        // no change
        assertEquals(10, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(10, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());

        // changed
        assertEquals(60 + PositionUtils.MODEL_PADDING, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(10 + 50 / 2 - 10, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(20, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(20, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());

        assertEquals(60 + PositionUtils.MODEL_PADDING + 20 + PositionUtils.MODEL_PADDING, m3.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(10, m3.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m3.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m3.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());
    }

    @Test
    public void testAlignModelsAllOverlap() {
        ModelUxf m1 = new ModelUxf();
        ElementUxf e1 = new ElementUxf();
        e1.setUmletPosition(new UmletPositionUxf(110, 210, 50, 50));
        m1.setElements(Set.of(e1));

        ModelUxf m2 = new ModelUxf();
        ElementUxf e2 = new ElementUxf();
        e2.setUmletPosition(new UmletPositionUxf(100, 200, 50, 50));
        m2.setElements(Set.of(e2));

        PositionUtils.alignModels(List.of(m1, m2));

        // no change
        assertEquals(110, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(210, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m1.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());

        // changed
        assertEquals(110 + 50 + PositionUtils.MODEL_PADDING, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getX());
        assertEquals(210, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getY());
        assertEquals(50, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getWidth());
        assertEquals(50, m2.getElements().stream().findFirst().orElseThrow().getUmletPosition().getHeight());
    }
}
