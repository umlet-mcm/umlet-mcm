package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

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
        exp1.setOriginalText(testAttributes1);

        exp1.setDescription("""
                Laser Welding Quality Control
                Cause-Effect Network and
                Product-Process-Resource Asset Network
                text
                """);

        HashMap<String, Object> attrs = new HashMap<>();
        attrs.put("type", "PAN");
        attrs.put("id", "1");
        ArrayList<String> tags = new ArrayList<>();
        tags.add("t1");
        attrs.put("tags", tags);
        attrs.put("custom", "value");
        exp1.setMcmAttributes(attrs);
    }

    @Test
    void testUnmarshal() {
        var res = testAdapter.unmarshal(testAttributes1);
        Assertions.assertEquals(exp1.getOriginalText(), res.getOriginalText());
        Assertions.assertEquals(exp1.getDescription(), res.getDescription());
        Assertions.assertEquals(exp1.getMcmAttributes(), res.getMcmAttributes());

        Assertions.assertNull(testAdapter.unmarshal(testAttributes2).getMcmAttributes());
    }

    @Test
    void testMarshal(){
        Assertions.assertEquals(testAttributes1, testAdapter.marshal(exp1));
    }

}
