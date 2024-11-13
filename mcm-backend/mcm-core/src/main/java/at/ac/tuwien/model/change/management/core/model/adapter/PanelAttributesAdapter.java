package at.ac.tuwien.model.change.management.core.model.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// The panel attributes are located in the "panel_attributes" element in a single string,
// lines that contain an attribute are prefixed with "//"
@Slf4j
public class PanelAttributesAdapter extends XmlAdapter<String, Map<String, Object>> {
    @Override
    public Map<String, Object> unmarshal(String panelAttributes) {

        var props = new HashMap<String, Object>();
        if (panelAttributes == null) {
            return new HashMap<>();
        }

        var lines = panelAttributes.split("\n");
        if (lines.length == 0) {
            return new HashMap<>();
        }

        for (var line : lines) {
            if (!line.startsWith("//") || line.length() <= 3) {
                continue;
            }

            var prop = getProperty(line.substring(2).trim()); // remove the leading "//"
            if (prop == null) {
                continue;
            }
            props.put((String) prop[0], prop[1]);
        }
        return props;
    }

    // Extract key-value pair from the input
    // Format: "key: value"
    // Values separated by "," are parsed into a list
    // The values are automatically converted into ints and floats when possible
    private Object[] getProperty(String s) {
        if (s.isEmpty()) {
            return null;
        }

        Object[] res = new Object[2];

        var kv = s.split(":");
        if (kv.length != 2) {
            log.warn("Could not parse panel attribute: '" + s + "'");
            return null;
        }

        res[0] = kv[0].trim();
        // check if the value is a list
        if (kv[1].contains(",")) {
            var values = kv[1].split(",");
            // try to parse the values and covert to list
            res[1] = Arrays.stream(values).map(v -> parseString(v.trim())).toList();
        } else {
            res[1] = parseString(kv[1].trim());
        }

        return res;
    }

    // Try to parse the input into an int or a float
    private Object parseString(String in) {
        if (StringUtils.isAlpha(in)) {
            return in;
        }

        try {
            return Integer.parseInt(in);
        } catch (NumberFormatException ignored) {
        }

        try {
            return Float.parseFloat(in);
        } catch (NumberFormatException ignored) {
        }

        return in;
    }

    // Not used currently, this can be later implemented if we want to serialize
    // the Java classes directly to XML
    @Override
    public String marshal(Map<String, Object> v) throws Exception {
        return null;
    }
}
