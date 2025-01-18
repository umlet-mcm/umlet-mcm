package at.ac.tuwien.model.change.management.core.model.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ParserUtils {
    public static final String ATTRIBUTE_VALUE_DELIM = ",";

    /**
     * Get MCM attributes from the commented out lines. Such lines start with "//" and each line
     * contains a "key: value" pair. Multiple values are parsed into lists, floats and ints are
     * converted automatically.
     *
     * @param text The text that contains the commented out attributes.
     * @return Attributes as key-value pairs.
     */
    public static LinkedHashMap<String, Object> extractAttributesFromComments(String text) {
        LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (text == null || text.isEmpty()) {
            return attrs;
        }

        var lines = text.split("\n");

        for (var line : lines) {
            line = line.stripLeading();
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
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        if (text == null || text.isEmpty()) {
            return attributes;
        }

        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.stripLeading();
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

        if (!s.contains(":")) {
            return null;
        }

        Object[] res = new Object[2];
        var kv = s.split(":");
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
     * Try to parse the input into an int, float or string.
     *
     * @param in The string to be parsed.
     * @return The result as int or float for if the value of the input was numerical. If the input was a simple
     * string it will be returned with the quotes removed. Inputs not belonging to any category are kept as strings
     * and not modified.
     */
    public static Object tryParseString(String in) {
        if (((in.startsWith("\"") && in.endsWith("\"")) ||
                (in.startsWith("`") && in.endsWith("`")))
                && in.length() > 1) {
            // the value was originally a string, remove the leading and trailing " or `
            return in.substring(1, in.length() - 1);
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

    /**
     * Extract the first block of text from the description until the first
     * horizontal line ("--", "-.", "-..")
     */
    public static String getTitle(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }

        var blocks = description.split("-{1,2}|-\\.\\.?");
        return blocks[0];
    }

    /**
     * Removes the title from the description
     */
    public static String getDescription(String textWithTitle) {
        String title = getTitle(textWithTitle);
        if (!title.isEmpty()) {
            return textWithTitle.replace(title, "");
        }

        return textWithTitle;
    }
}
