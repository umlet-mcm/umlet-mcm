package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Point;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RelationUtilsTest {

    @Test
    void isConnectedTest() {
        Node n = new Node();
        UMLetPosition pos = new UMLetPosition(10, 20, 50, 30);
        n.setUmletPosition(pos);

        // test edges
        Point p = new Point(15, 20);
        Assertions.assertTrue(RelationUtils.isConnected(n, p, 0));

        p = new Point(10, 25);
        Assertions.assertTrue(RelationUtils.isConnected(n, p, 0));

        p = new Point(60, 25);
        Assertions.assertTrue(RelationUtils.isConnected(n, p, 0));

        p = new Point(40, 50);
        Assertions.assertTrue(RelationUtils.isConnected(n, p, 0));

        // test inside
        p = new Point(27, 32);
        Assertions.assertTrue(RelationUtils.isConnected(n, p, 0));

        p = new Point(48, 22);
        Assertions.assertTrue(RelationUtils.isConnected(n, p, 0));

        // test outside
        p = new Point(61, 32);
        Assertions.assertFalse(RelationUtils.isConnected(n, p, 0));

        p = new Point(70, 60);
        Assertions.assertFalse(RelationUtils.isConnected(n, p, 0));
    }
    
    @Test
    void splitLineTypeTest() {
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
        String[] exp10 = {"..", ">>"};

        String in11 = ".";
        String[] exp11 = {"."};

        String in12 = "asd.";

        Assertions.assertArrayEquals(exp1, RelationUtils.splitLineType(in1));
        Assertions.assertArrayEquals(exp2, RelationUtils.splitLineType(in2));
        Assertions.assertArrayEquals(exp3, RelationUtils.splitLineType(in3));
        Assertions.assertArrayEquals(exp4, RelationUtils.splitLineType(in4));
        Assertions.assertArrayEquals(exp5, RelationUtils.splitLineType(in5));
        Assertions.assertArrayEquals(exp6, RelationUtils.splitLineType(in6));
        Assertions.assertArrayEquals(exp7, RelationUtils.splitLineType(in7));
        Assertions.assertArrayEquals(exp8, RelationUtils.splitLineType(in8));
        Assertions.assertArrayEquals(exp9, RelationUtils.splitLineType(in9));
        Assertions.assertArrayEquals(exp10, RelationUtils.splitLineType(in10));
        Assertions.assertArrayEquals(exp11, RelationUtils.splitLineType(in11));
        Assertions.assertNull(RelationUtils.splitLineType(in12));
    }

    @Test
    void joinLineTypeTest() {
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


        assertEquals(exp1, RelationUtils.joinLineTypes(in1a, in1b));
        assertEquals(exp2, RelationUtils.joinLineTypes(in2a, in2b));
        assertEquals(exp3, RelationUtils.joinLineTypes(in3a, in3b));
        assertEquals(exp4, RelationUtils.joinLineTypes(in4a, in4b));
        assertEquals(exp5, RelationUtils.joinLineTypes(in5a, in5b));
        assertEquals(exp6, RelationUtils.joinLineTypes(in6a, in6b));
        assertEquals(exp7, RelationUtils.joinLineTypes(in7a, in7b));
        assertEquals(exp8, RelationUtils.joinLineTypes(in8a, in8b));
        assertEquals(exp9, RelationUtils.joinLineTypes(in9a, in9b));
        assertEquals(exp10, RelationUtils.joinLineTypes(in10a, in10b));

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

    @Test
    void processRelationsTest(){
        Node n1 = new Node();
        n1.setElementType("Node");
        n1.setUmletPosition(new UMLetPosition(0,0,30,30));

        Node n2 = new Node();
        n2.setElementType("Node");
        n2.setUmletPosition(new UMLetPosition(50,50,100,100));

        UMLetPosition pos1 = new UMLetPosition(10,10,20,20);
        UMLetPosition pos2 = new UMLetPosition(10,20,10,90);
        UMLetPosition pos3 = new UMLetPosition(60,60,100,60);
        Node r1 = getRelationNode("id1", pos1, "<->", List.of(0,0,60,60));
        Node r2 = getRelationNode("id2", pos2, "-", List.of(0,0,60,70));
        Node r3 = getRelationNode("id3", pos3, "<-", List.of(0,0,-50,-50));

        Model model = new Model();
        model.setNodes(new LinkedHashSet<>());
        model.getNodes().add(n1);
        model.getNodes().add(n2);
        model.getNodes().add(r1);
        model.getNodes().add(r2);
        model.getNodes().add(r3);

        Model res = RelationUtils.processRelations(model);
        assertEquals(2, res.getNodes().size()); // the relations should no longer be present as nodes
        assertNotNull(n1.getRelations());
        assertNotNull(n2.getRelations());
        assertEquals(3, n1.getRelations().size());
        assertEquals(2, n2.getRelations().size());
    }

    Node getRelationNode(String id, UMLetPosition pos, String lineType, List<Integer> genAttribs){
        Node res = new Node();
        res.setElementType("Relation");
        res.setId(id);
        res.setUmletPosition(pos);
        res.setUmletAttributes(new LinkedHashMap<>());
        res.getUmletAttributes().put(AttributeKeys.LINE_TYPE, lineType);
        res.setGeneratedAttributes(genAttribs);

        return res;
    }
}
