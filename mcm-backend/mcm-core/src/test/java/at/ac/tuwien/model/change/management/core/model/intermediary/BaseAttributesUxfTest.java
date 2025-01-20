package at.ac.tuwien.model.change.management.core.model.intermediary;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseAttributesUxfTest {

    @Test
    void testToString() {
        BaseAttributesUxf attr = new BaseAttributesUxf();
        attr.setDescription("Description");
        LinkedHashMap<String, Object> mcmAttrs = new LinkedHashMap<>();
        mcmAttrs.put("key1", "val");
        mcmAttrs.put("key2", 1);
        mcmAttrs.put("key3", List.of(1, 2, 3, 4));
        mcmAttrs.put("key4", List.of("s1", "s2"));
        mcmAttrs.put("key5", null);
        attr.setMcmAttributes(mcmAttrs);

        LinkedHashMap<String, String> inlineComments = new LinkedHashMap<>();
        inlineComments.put("key1", "// comment1");
        inlineComments.put("key4", "//comment2");
        attr.setMcmAttributesInlineComments(inlineComments);

        String exp = """
                Description
                // key1: "val" // comment1
                // key2: 1
                // key3: 1, 2, 3, 4
                // key4: "s1", "s2" //comment2
                """;
        assertEquals(exp, attr.toString());
    }
}
