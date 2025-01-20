package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ElementAttributesAdapterTest {

    private ElementAttributesAdapter testAdapter = new ElementAttributesAdapter();
    private static String testAttributes1 = """
            // -----------
            <<EQIP>>
            AssemblyStation
            // -----------
            // id: "Resource Equipment ID Default"
            description
            // tags: "t1", "t2"
            // -----------
            // eqipID: 1
            bg=#ccccff
            // light violet
            // -----------
            """;

    private static ElementAttributesUxf exp1 = new ElementAttributesUxf();

    @BeforeAll
    static void init() {
        exp1.setDescription("""
                // -----------
                <<EQIP>>
                AssemblyStation
                // -----------
                description
                // -----------
                // light violet
                // -----------
                """);

        LinkedHashMap<String, Object> mcmAttrs = new LinkedHashMap<>();
        mcmAttrs.put("id", "Resource Equipment ID Default");
        ArrayList<String> tags = new ArrayList<>();
        tags.add("t1");
        tags.add("t2");
        mcmAttrs.put("tags", tags);
        mcmAttrs.put("eqipID", 1);
        exp1.setMcmAttributes(mcmAttrs);

        HashMap<String, String> umletAttrs = new HashMap<>();
        umletAttrs.put("bg", "#ccccff");
        exp1.setUmletAttributes(umletAttrs);
    }

    @Test
    void testUnmarshal() {
        var res = testAdapter.unmarshal(testAttributes1);
        Assertions.assertEquals(exp1.getDescription(), res.getDescription());
        Assertions.assertEquals(exp1.getMcmAttributes(), res.getMcmAttributes());
        Assertions.assertEquals(exp1.getUmletAttributes(), res.getUmletAttributes());
        Assertions.assertNotNull(testAdapter.unmarshal(""));
    }
}
