package at.ac.tuwien.model.change.management.core.model.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ParserUtils {
    private static final String ATTRIBUTE_VALUE_DELIM = ",";

    /**
     * Get MCM attributes from the commented out lines. Such lines start with "//" and each line
     * contains a "key: value" pair. Multiple values are parsed into lists, floats and ints are
     * converted automatically.
     *
     * @param text The text that contains the commented out attributes.
     * @return Attributes as key-value pairs.
     */
    public static Map<String, Object> extractAttributesFromComments(String text) {
        HashMap<String, Object> attrs = new HashMap<>();
        if (text == null || text.isEmpty()) {
            return attrs;
        }

        var lines = text.split("\n");

        for (var line : lines) {
            if (!line.startsWith("//") || line.length() <= 3) {
                continue;
            }

            var prop = getMcmKeyValue(line.substring(2).trim()); // remove the leading "//"
            if (prop == null) {
                continue;
            }
            attrs.put((String) prop[0], prop[1]);
        }

        return attrs;
    }

    /**
     * Get all lines from a text that are not comments or Umlet key=value pairs.
     *
     * @param textWithComments The original text.
     * @return The extracted lines as one string.
     */
    public static String extractText(String textWithComments) {
        if (textWithComments == null || textWithComments.isEmpty()) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        String[] lines = textWithComments.split("\n");
        for (String line : lines) {
            if (!line.startsWith("//") && !line.contains("=")) {
                out.append(line + "\n");
            }
        }
        return out.toString();
    }

    /**
     * Extract Umlet attributes in the form of "key=value" pairs from a text.
     *
     * @param text The original text.
     * @return Key-value pairs of the attributes, values are kept as strings.
     */
    public static Map<String, String> extractUmletAttributes(String text) {
        HashMap<String, String> attributes = new HashMap<>();
        if (text == null || text.isEmpty()) {
            return attributes;
        }

        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("//")) {
                continue;
            }
            String[] kv = line.split("=");
            if (kv.length == 2) {
                attributes.put(kv[0], kv[1]);
            }
        }

        return attributes;
    }

    /**
     * Extract key-value pair from the input that match the format "key: value". Values separated
     * by "," are parsed into a list. The values are automatically converted into ints and floats
     * when possible.
     *
     * @param s The string that might contain a valid key-value.
     * @return The extracted key-value pair or null if no valid attribute was found.
     */
    private static Object[] getMcmKeyValue(String s) {
        if (s.isEmpty()) {
            return null;
        }

        Object[] res = new Object[2];

        var kv = s.split(":");
        if (kv.length == 0) {
            log.warn("Could not parse panel attribute: '" + s + "', no <key: value> pair found");
            return null;
        }
        if (kv.length == 1) {
            log.warn("Could not parse panel attribute: '" + s + "', no value found for key '" + kv[0] + "'");
            return null;
        }

        res[0] = kv[0].trim();
        // check if the value is a list
        if (kv[1].contains(ATTRIBUTE_VALUE_DELIM)) {
            var values = kv[1].split(ATTRIBUTE_VALUE_DELIM);
            // try to parse the values and covert to list
            res[1] = Arrays.stream(values).map(v -> ParserUtils.tryParseString(v.trim())).toList();
        } else {
            res[1] = ParserUtils.tryParseString(kv[1].trim());
        }

        return res;
    }

    /**
     * Try to parse the input into an int or a float.
     *
     * @param in The string to be parsed.
     * @return The result as int or float for a successful parsing, the original string for a failure.
     */
    public static Object tryParseString(String in) {
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
}
