package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Extract data from the panel_attributes element
 */
public class ElementAttributesAdapter extends XmlAdapter<String, ElementAttributesUxf> {
    @Override
    public ElementAttributesUxf unmarshal(String text) {
        ElementAttributesUxf attributes = new ElementAttributesUxf();
        if (text == null || text.isEmpty()) {
            return attributes;
        }

        attributes.setDescription(ParserUtils.extractText(text));
        attributes.setMcmAttributes(ParserUtils.extractAttributesFromComments(text));
        attributes.setUmletAttributes(ParserUtils.extractUmletAttributes(text));

        return attributes;
    }

    // todo
    @Override
    public String marshal(ElementAttributesUxf v) {
        return null;
    }
}
