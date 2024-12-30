package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;

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


        LinkedHashMap<String, Object> attrs = ParserUtils.extractAttributesFromComments(text);
        attributes.setMcmAttributes(attrs);
        return attributes;
    }

    @Override
    public String marshal(BaseAttributesUxf v) {
        return v.toString().trim();
    }
}
