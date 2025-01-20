package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Extract data from the panel_attributes element
 */
@Slf4j
public class ElementAttributesAdapter extends XmlAdapter<String, ElementAttributesUxf> {
    @Override
    public ElementAttributesUxf unmarshal(String text) {
        ElementAttributesUxf attributes = new ElementAttributesUxf();
        if (text == null || text.isEmpty()) {
            return attributes;
        }

        attributes.setDescription(ParserUtils.extractText(text));
        attributes.setUmletAttributes(ParserUtils.extractUmletAttributes(text));
        ParserUtils.populateMcmAttributesAndInlineComments(attributes, text);

        return attributes;
    }

    @Override
    public String marshal(ElementAttributesUxf v) {
        return v.toString().trim();
    }
}
