package at.ac.tuwien.model.change.management.core.model.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParserUtilsTest {

    @Test
    void extractAttributesFromCommentsTest() {
        String in1 = """
                Title
                // ck1: "val1"
                // ck2: 123 // comment
                // ------
                // a:
                //
                // ck4: asd
                // ck5: "val", 12, asd
                fg=#ffffff
                """;

        var exp1 = new LinkedHashMap<String, Pair<Object, String>>();
        exp1.put("ck1", new ImmutablePair<>("val1", null));
        exp1.put("ck2", new ImmutablePair<>(123, "// comment"));
        exp1.put("ck4", new ImmutablePair<>("asd", null));
        exp1.put("ck5", new ImmutablePair<>(List.of("val", 12, "asd"), null));

        Assertions.assertDoesNotThrow(() -> ParserUtils.extractAttributesFromComments(in1));

        Assertions.assertEquals(exp1, ParserUtils.extractAttributesFromComments(in1));

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
