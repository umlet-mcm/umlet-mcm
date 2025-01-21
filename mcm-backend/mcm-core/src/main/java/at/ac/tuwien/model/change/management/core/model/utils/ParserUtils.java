package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParserUtils {
    public static final String ATTRIBUTE_VALUE_DELIM = ",";
    /**
     * Matches mcm attribute declarations even if they have an inline comment
     * The inline comment is not extractable from this pattern
     */
    public static final Pattern MCM_ATTRIBUTE_DECLARATION_PATTERN = Pattern.compile("^\\/\\/\\s*([a-zA-Z_][a-zA-Z0-9_-]*)\\s*:(.*)");

    /**
     * Only matches mcm attribute declarations that have an inline comment
     * The inline comment is extractable from this pattern as capture group
     */
    public static final Pattern MCM_ATTRIBUTE_DECLARATION_PATTERN_WITH_INLINE_COMMENT = Pattern.compile("^\\/\\/\\s*([a-zA-Z_][a-zA-Z0-9_-]*)\\s*:(.*)(\\/\\/.*)");

    /**
     * Get MCM attributes and inline comments from the commented out lines. Such lines start with "//" and each line
     * contains a "key: value" pair. Multiple values are parsed into lists, floats and ints are
     * converted automatically.
     *
     * @param text The text that contains the commented out attributes.
     * @return Map of attributes, key: attribute key, value: a pair of the value for the attribute and the inline comment.
     */
    public static LinkedHashMap<String, Pair<Object, String>> extractAttributesFromComments(String text) {
        LinkedHashMap<String, Pair<Object, String>> attrs = new LinkedHashMap<>();
        if (text == null || text.isEmpty()) {
            return attrs;
        }

        var lines = text.split("\n");

        for (var line : lines) {
            line = line.stripLeading();
            if (!line.startsWith("//") || line.length() <= 3) {
                continue;
            }

            Object[] prop = getMcmKeyValueInlineComment(line.trim());
            if (prop == null) {
                continue;
            }

            if (prop.length != 3) {
                continue; // shouldn't happen
            }

            attrs.put((String) prop[0], new ImmutablePair<>(prop[1], (String) prop[2]));
        }

        return attrs;
    }

    /**
     * Extract and parse mcm attributes and the inline comments
     *
     * @param attributes target where the attributes and the inline comments should be stored
     * @param text       raw text that can contain mcm attributes
     */
    public static void populateMcmAttributesAndInlineComments(BaseAttributesUxf attributes, String text) {
        LinkedHashMap<String, Pair<Object, String>> attrs = ParserUtils.extractAttributesFromComments(text);
        LinkedHashMap<String, Object> attrsNoComments = new LinkedHashMap<>();
        LinkedHashMap<String, String> attrsComments = new LinkedHashMap<>();
        // split into two maps
        for (var kv : attrs.entrySet()) {
            attrsNoComments.put(kv.getKey(), kv.getValue().getLeft());
            if (kv.getValue().getRight() != null) {
                attrsComments.put(kv.getKey(), kv.getValue().getRight());
            }
        }
        attributes.setMcmAttributes(attrsNoComments);
        attributes.setMcmAttributesInlineComments(attrsComments);
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
                out.append(line).append("\n");
            } else if (line.startsWith("//")) {
                Matcher matcher = MCM_ATTRIBUTE_DECLARATION_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    out.append(line).append("\n"); // line is a simple comment, add to output
                }
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
    public static Object[] getMcmKeyValueInlineComment(String s) {
        if (s.isEmpty()) {
            return null;
        }

        if (!s.contains(":")) {
            return null;
        }

        Object[] res = new Object[3];

        Matcher matcherWithIC = MCM_ATTRIBUTE_DECLARATION_PATTERN_WITH_INLINE_COMMENT.matcher(s);

        String key;
        String value;
        String inlineComment;

        if (matcherWithIC.matches()) {
            key = matcherWithIC.group(1).trim();
            value = matcherWithIC.group(2);
            inlineComment = matcherWithIC.group(3);
        } else {
            Matcher matcher = MCM_ATTRIBUTE_DECLARATION_PATTERN.matcher(s);
            if (!matcher.matches()) {
                return null;
            }

            key = matcher.group(1).trim();
            value = matcher.group(2);
            inlineComment = null;
        }

        if (value == null || value.isEmpty()) {
            log.warn("Could not parse panel attribute: '{}', no value found for key '{}'", s, key);
            return null;
        }

        res[0] = key;
        // check if the value is a list
        if (value.contains(ATTRIBUTE_VALUE_DELIM)) {
            var values = value.split(ATTRIBUTE_VALUE_DELIM);
            // try to parse the values and covert to list
            res[1] = Arrays.stream(values).map(v -> ParserUtils.tryParseString(v.trim())).toList();
        } else {
            res[1] = ParserUtils.tryParseString(value.trim());
        }

        res[2] = inlineComment; // null if there's no inline comment

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

        var blocks = description.split("^-{1,2}|-\\.\\.?");
        return blocks[0].trim();
    }

    /**
     * Removes the title from the description
     */
    public static String getDescription(String textWithTitle) {
        String title = getTitle(textWithTitle);
        if (!title.isEmpty()) {
            return textWithTitle.replaceFirst(title, "").trim();
        }

        return textWithTitle;
    }

    /**
     * Wrap strings in "", turn lists into the delimiter separated string form.
     * Return toString for other objects.
     */
    public static String formatMcmValueForExport(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return "\"" + value + "\"";
        }

        if (value instanceof List<?>) {
            StringBuilder sb = new StringBuilder();
            for (Object item : (List<?>) value) {
                if (item instanceof String) { // strings should have enclosing "" when exported to uxf
                    sb.append("\"" + item + "\"");
                } else {
                    sb.append(item);
                }
                sb.append(ATTRIBUTE_VALUE_DELIM + " ");
            }

            if (!((List<?>) value).isEmpty()) {
                sb.delete(sb.length() - 2, sb.length()); // remove trailing ", "
            }

            return sb.toString();
        }

        return value.toString();
    }

}
