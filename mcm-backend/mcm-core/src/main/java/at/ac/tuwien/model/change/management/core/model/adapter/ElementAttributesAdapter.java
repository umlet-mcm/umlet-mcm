package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.attributes.McmAttributesException;
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

        attributes.setOriginalText(text);
        attributes.setDescription(ParserUtils.extractText(text));
        attributes.setUmletAttributes(ParserUtils.extractUmletAttributes(text));
        try {
            attributes.setMcmAttributes(ParserUtils.extractAttributesFromComments(text, false));
        } catch (McmAttributesException e) {
            log.error("Failed to parse attributes: " + e.getMessage());
            return attributes;
        }

        return attributes;
    }

    @Override
    public String marshal(ElementAttributesUxf v) {
        return v.toString();
    }
}
