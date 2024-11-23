package at.ac.tuwien.model.change.management.core.model.adapter;

import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Extract values from the additional_attributes element
 */
public class AdditionalAttributesAdapter extends XmlAdapter<String, List<Object>> {
    private static final String ATTRIBUTE_VALUE_DELIM = ";";

    @Override
    public List<Object> unmarshal(String v) {
        if (v == null || v.isEmpty()) {
            return null;
        }

        ArrayList<Object> attributes = new ArrayList<>();
        String[] vals = v.split(ATTRIBUTE_VALUE_DELIM);
        for (String val : vals) {
            attributes.add(ParserUtils.tryParseString(val));
        }

        return attributes;
    }

    // todo
    @Override
    public String marshal(List<Object> v) {
        return null;
    }
}
