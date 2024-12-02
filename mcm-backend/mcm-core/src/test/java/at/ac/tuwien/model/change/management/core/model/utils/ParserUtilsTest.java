package at.ac.tuwien.model.change.management.core.model.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserUtilsTest {

    @Test
    void extractAttributesFromCommentsNoKeyCheckTest() {
        String in1 = """
                Title
                // ck1: "val1"
                // ck2: 123
                // ------
                // a:
                //
                // ck4: asd
                // ck5: "val", 12, asd
                fg=#ffffff
                """;

        var exp1 = new HashMap<>();
        exp1.put("ck1", "val1");
        exp1.put("ck2", 123);
        exp1.put("ck4", "asd");
        exp1.put("ck5", List.of("val", 12, "asd"));

        Assertions.assertDoesNotThrow(() -> ParserUtils.extractAttributesFromComments(in1));

        Assertions.assertEquals(exp1, ParserUtils.extractAttributesFromComments(in1));

    }

    @Test
    void extractAttributesFromCommentsKeyCheckTest() {
        String in3 = """
                // id: "a2bf3"
                // tags: "t1", "t2"
                """;

        HashMap<String, Object> exp3 = new HashMap<>();
        exp3.put("id", "a2bf3");
        ArrayList<String> tags = new ArrayList<>();
        tags.add("t1");
        tags.add("t2");
        exp3.put("tags", tags);

        Assertions.assertDoesNotThrow(() -> ParserUtils.extractAttributesFromComments(in3));
        Assertions.assertEquals(exp3, ParserUtils.extractAttributesFromComments(in3));
        String in4 = """
                // id: "a2bf3"
                // tags: "t1"
                """;
        HashMap<String, Object> exp4 = new HashMap<>();
        exp4.put("id", "a2bf3");
        ArrayList<String> tags2 = new ArrayList<>();
        tags2.add("t1");
        exp4.put("tags", tags2);

        Assertions.assertDoesNotThrow(() -> ParserUtils.extractAttributesFromComments(in4));

        Assertions.assertEquals(exp4, ParserUtils.extractAttributesFromComments(in4));

    }

    @Test
    void extractTextTest() {
        String in1 = "";

        Assertions.assertEquals(in1, ParserUtils.extractText(in1));

        String in2 = """
                Title
                // id: "asd"
                fg=#ffffff
                --
                +Method
                """;

        String exp2 = """
                Title
                --
                +Method
                """;

        Assertions.assertEquals(exp2, ParserUtils.extractText(in2));
    }

    @Test
    void extractUmletAttributesTest() {
        String in1 = "P0 Preparation \n" +
                "// PPR Type: Process Phase\n" +
                "// --\n" +
                "// Id: \n" +
                "// Version: 0.1\n" +
                "// Tags: \n" +
                "fg=#000000\n" +
                "// Property: \n" +
                "\n" +
                "bg=#a4a4f7";

        var exp1 = Map.ofEntries(
                Map.entry("fg", "#000000"),
                Map.entry("bg", "#a4a4f7")
        );

        String in2 = "";
        var exp2 = new HashMap<String, String>();

        Assertions.assertEquals(exp1, ParserUtils.extractUmletAttributes(in1));
        Assertions.assertEquals(exp2, ParserUtils.extractUmletAttributes(in2));
    }

    @Test
    void testParseString() {
        String in1 = "\"string2345\"";
        String exp1 = "string2345";
        String in2 = "\"\"";
        String exp2 = "";
        String in3 = "\"11\"";
        String exp3 = "11";
        String in4 = "`a \"b\" c`";
        String exp4 = "a \"b\" c";

        Object res;
        res = ParserUtils.tryParseString(in1);
        Assertions.assertEquals(res, exp1);

        res = ParserUtils.tryParseString(in2);
        Assertions.assertEquals(res, exp2);

        res = ParserUtils.tryParseString(in3);
        Assertions.assertEquals(res, exp3);

        res = ParserUtils.tryParseString(in4);
        Assertions.assertEquals(res, exp4);
    }

    @Test
    void testParseInt() {
        String in1 = "32";
        int exp1 = 32;
        String in2 = "-4";
        int exp2 = -4;

        Object res;
        res = ParserUtils.tryParseString(in1);
        Assertions.assertEquals(res, exp1);

        res = ParserUtils.tryParseString(in2);
        Assertions.assertEquals(res, exp2);
    }

    @Test
    void testParseFloat() {
        String in1 = "0.9";
        float exp1 = 0.9f;
        String in2 = "-17.0";
        float exp2 = -17.0f;
        String in3 = "0.0";
        float exp3 = 0.0f;

        Object res;
        res = ParserUtils.tryParseString(in1);
        Assertions.assertEquals(res, exp1);

        res = ParserUtils.tryParseString(in2);
        Assertions.assertEquals(res, exp2);

        res = ParserUtils.tryParseString(in3);
        Assertions.assertEquals(res, exp3);
    }

    @Test
    void testNoParse() {
        String in1 = "";
        String in2 = "\"";
        String in3 = "string12";

        Object res;
        res = ParserUtils.tryParseString(in1);
        Assertions.assertEquals(res, in1);

        res = ParserUtils.tryParseString(in2);
        Assertions.assertEquals(res, in2);

        res = ParserUtils.tryParseString(in3);
        Assertions.assertEquals(res, in3);
    }
}
