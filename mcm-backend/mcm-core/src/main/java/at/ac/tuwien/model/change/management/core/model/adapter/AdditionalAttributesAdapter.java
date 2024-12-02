package at.ac.tuwien.model.change.management.core.model.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Extract values from the additional_attributes element
 */
public class AdditionalAttributesAdapter extends XmlAdapter<String, List<Integer>> {
    private static final String ATTRIBUTE_VALUE_DELIM = ";";

    @Override
    public List<Integer> unmarshal(String v) {
        if (v == null || v.isEmpty()) {
            return null;
        }

        ArrayList<Integer> attributes = new ArrayList<>();
        String[] vals = v.split(ATTRIBUTE_VALUE_DELIM);
        for (String val : vals) {
            // sometimes the ints are stored as floats (1.0)
            attributes.add(Math.round(Float.parseFloat(val)));
        }

        return attributes;
    }

    @Override
    public String marshal(List<Integer> v) {
        StringBuilder sb = new StringBuilder();
        for (Object o : v) {
            sb.append(o);
            sb.append(ATTRIBUTE_VALUE_DELIM);
        }
        sb.deleteCharAt(sb.length() - 1); // remove trailing delimiter

        return sb.toString();
    }
}
