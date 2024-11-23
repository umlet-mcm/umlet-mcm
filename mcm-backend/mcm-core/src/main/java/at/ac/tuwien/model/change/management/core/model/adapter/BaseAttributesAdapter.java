package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.Map;

/**
 * Extract data from the help_text element
 */
public class BaseAttributesAdapter extends XmlAdapter<String, BaseAttributesUxf> {
    @Override
    public BaseAttributesUxf unmarshal(String text) {
        BaseAttributesUxf attributes = new BaseAttributesUxf();
        if (text == null || text.isEmpty()) {
            return attributes;
        }

        String description = ParserUtils.extractText(text);
        attributes.setDescription(description);

        Map<String, Object> attrs = ParserUtils.extractAttributesFromComments(text);
        attributes.setMcmAttributes(attrs);

        return attributes;
    }

    // todo
    @Override
    public String marshal(BaseAttributesUxf v) {
        return null;
    }
}
