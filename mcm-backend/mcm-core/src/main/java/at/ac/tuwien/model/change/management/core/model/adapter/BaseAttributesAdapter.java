package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.attributes.McmAttributesException;
import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * Extract data from the help_text element
 */
@Slf4j
public class BaseAttributesAdapter extends XmlAdapter<String, BaseAttributesUxf> {
    @Override
    public BaseAttributesUxf unmarshal(String text) {
        BaseAttributesUxf attributes = new BaseAttributesUxf();
        if (text == null || text.isEmpty()) {
            return attributes;
        }

        attributes.setOriginalText(text);

        String description = ParserUtils.extractText(text);
        attributes.setDescription(description);

        try {
            HashMap<String, Object> attrs = ParserUtils.extractAttributesFromComments(text, false);
            attributes.setMcmAttributes(attrs);
        } catch (McmAttributesException e) {
            log.error("Failed to parse attributes: " + e.getMessage());
            return attributes;
        }


        return attributes;
    }

    @Override
    public String marshal(BaseAttributesUxf v) {
        return v.toString();
    }
}
