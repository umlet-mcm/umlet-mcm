package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Point;
import at.ac.tuwien.model.change.management.core.model.UmletPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RelationUtilsTest {

    @Test
    void isConnectedTest(){
        Node n = new Node();
        UmletPosition pos = new UmletPosition();
        pos.setX(10);
        pos.setY(20);
        pos.setWidth(50);
        pos.setHeight(30);
        n.setUmletPosition(pos);

        // test edges
        Point p = new Point(15, 20);
        Assertions.assertTrue(RelationUtils.isConnected(n,p));

        p = new Point(10, 25);
        Assertions.assertTrue(RelationUtils.isConnected(n,p));

        p = new Point(60, 25);
        Assertions.assertTrue(RelationUtils.isConnected(n,p));

        p = new Point(40, 50);
        Assertions.assertTrue(RelationUtils.isConnected(n,p));

        // test inside
        p = new Point(27, 32);
        Assertions.assertTrue(RelationUtils.isConnected(n,p));

        p = new Point(48, 22);
        Assertions.assertTrue(RelationUtils.isConnected(n,p));

        // test outside
        p = new Point(61, 32);
        Assertions.assertFalse(RelationUtils.isConnected(n,p));

        p = new Point(70, 60);
        Assertions.assertFalse(RelationUtils.isConnected(n,p));
    }

    @Test
    void splitLineTypeTest(){
        String in1 = "<<.[a]";
        String[] exp1 = {"<<", ".", "[a]"};

        String in2 = "<<..";
        String[] exp2 = {"<<", ".."};

        String in3 = "[Qua.lification]<-";
        String[] exp3 = {"[Qua.lification]<", "-"};

        String in4 = "<<<<->>";
        String[] exp4 = {"<<<<", "-", ">>"};

        String in5 = "[asd]<<<-";
        String[] exp5 = {"[asd]<<<", "-"};

        String in6 = "[asd]<<<-(+)";
        String[] exp6 = {"[asd]<<<", "-", "(+)"};

        String in7 = "[asd]<<<-[asd]";
        String[] exp7 = {"[asd]<<<", "-", "[asd]"};

        String in8 = "[asd]<<<->>>[asd]";
        String[] exp8 = {"[asd]<<<", "-", ">>>[asd]"};

        String in9 = "(+)..>[asd]";
        String[] exp9 = {"(+)", "..", ">[asd]"};

        String in10 = "..>>";
        String[] exp10 = {"..",">>"};

        String in11 = ".";
        String[] exp11 = {"."};

        String in12 = "asd.";

        Assertions.assertArrayEquals(exp1,RelationUtils.splitLineType(in1));
        Assertions.assertArrayEquals(exp2,RelationUtils.splitLineType(in2));
        Assertions.assertArrayEquals(exp3,RelationUtils.splitLineType(in3));
        Assertions.assertArrayEquals(exp4,RelationUtils.splitLineType(in4));
        Assertions.assertArrayEquals(exp5,RelationUtils.splitLineType(in5));
        Assertions.assertArrayEquals(exp6,RelationUtils.splitLineType(in6));
        Assertions.assertArrayEquals(exp7,RelationUtils.splitLineType(in7));
        Assertions.assertArrayEquals(exp8,RelationUtils.splitLineType(in8));
        Assertions.assertArrayEquals(exp9,RelationUtils.splitLineType(in9));
        Assertions.assertArrayEquals(exp10,RelationUtils.splitLineType(in10));
        Assertions.assertArrayEquals(exp11,RelationUtils.splitLineType(in11));
        Assertions.assertNull(RelationUtils.splitLineType(in12));
    }

    @Test
    void joinLineTypeTest(){
        String in1a = "<<.";
        String in1b = ".[a]";
        String exp1 = "<<.[a]";

        String in2a = "..>";
        String in2b = "<<..";
        String exp2 = "<<..>";

        String in3a = "->|";
        String in3b = "[Qua.lification]<-";
        String exp3 = "[Qua.lification]<->|";

        String in4a = "<<<<-";
        String in4b = "->>";
        String exp4 = "<<<<->>";

        String in5a = "[asd]<<<-";
        String in5b = "->";
        String exp5 = "[asd]<<<->";

        String in6a = "-(+)";
        String in6b = "[asd]<<<-";
        String exp6 = "[asd]<<<-(+)";

        String in7a = "[asd]<<<-";
        String in7b = "-[asd]";
        String exp7 = "[asd]<<<-[asd]";

        String in8a = "[asd]<<<-";
        String in8b = "->>>[asd]";
        String exp8 = "[asd]<<<->>>[asd]";

        String in9a = "..>[asd]";
        String in9b = "(+)..";
        String exp9 = "(+)..>[asd]";

        String in10a = ".";
        String in10b = ".";
        String exp10 = ".";


        Assertions.assertEquals(exp1,RelationUtils.joinLineTypes(in1a, in1b));
        Assertions.assertEquals(exp2,RelationUtils.joinLineTypes(in2a, in2b));
        Assertions.assertEquals(exp3,RelationUtils.joinLineTypes(in3a, in3b));
        Assertions.assertEquals(exp4,RelationUtils.joinLineTypes(in4a, in4b));
        Assertions.assertEquals(exp5,RelationUtils.joinLineTypes(in5a, in5b));
        Assertions.assertEquals(exp6,RelationUtils.joinLineTypes(in6a, in6b));
        Assertions.assertEquals(exp7,RelationUtils.joinLineTypes(in7a, in7b));
        Assertions.assertEquals(exp8,RelationUtils.joinLineTypes(in8a, in8b));
        Assertions.assertEquals(exp9,RelationUtils.joinLineTypes(in9a, in9b));
        Assertions.assertEquals(exp10,RelationUtils.joinLineTypes(in10a, in10b));

        String in11a = "asd";
        String in11b = ".>>";
        Assertions.assertNull(RelationUtils.joinLineTypes(in11a, in11b));

        String in12a = ".";
        String in12b = ".>>";
        Assertions.assertNull(RelationUtils.joinLineTypes(in12a, in12b));

        String in13a = "<.>";
        String in13b = "<.>>";
        Assertions.assertNull(RelationUtils.joinLineTypes(in13a, in13b));
    }
}
