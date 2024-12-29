package at.ac.tuwien.model.change.management.core.model.intermediary;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElementAttributesUxfTest {

    @Test
    void testToString(){
        LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
        attrs.put("fg", "#ffffff");
        attrs.put("lt", "<<->>");
        attrs.put("key", "122");

        ElementAttributesUxf attributesUxf = new ElementAttributesUxf();
        attributesUxf.setUmletAttributes(attrs);
        attributesUxf.setDescription("Description");

        String res = """
                Description
                fg=#ffffff
                lt=<<->>
                key=122
                """;

        assertEquals(res, attributesUxf.toString());
    }
}
