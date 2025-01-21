package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseAttributesAdapterTest {

    private BaseAttributesAdapter testAdapter = new BaseAttributesAdapter();
    private static String testAttributes1 = """
            Laser Welding Quality Control
            Cause-Effect Network and
            Product-Process-Resource Asset Network
            // -----------
            // type: "PAN"
            // id: "1"
            text
            // tags: "t1"
            // custom: "value"
            """;

    // invalid id
    private static String testAttributes2 = """
            Title
            // id: 12
            """;

    private static BaseAttributesUxf exp1 = new BaseAttributesUxf();

    @BeforeAll
    static void init() {

        exp1.setDescription("""
                Laser Welding Quality Control
                Cause-Effect Network and
                Product-Process-Resource Asset Network
                // -----------
                text
                """);

        LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("type", "PAN");
        attrs.put("id", "1");
        attrs.put("tags", "t1"); // the conversion to list happens later
        attrs.put("custom", "value");
        exp1.setMcmAttributes(attrs);
    }

    @Test
    void testUnmarshal() {
        var res = testAdapter.unmarshal(testAttributes1);
        Assertions.assertEquals(exp1.getDescription(), res.getDescription());
        Assertions.assertEquals(exp1.getMcmAttributes(), res.getMcmAttributes());

        Assertions.assertEquals(Map.of("id", 12), testAdapter.unmarshal(testAttributes2).getMcmAttributes());

        Assertions.assertNotNull(testAdapter.unmarshal(""));
    }
}
