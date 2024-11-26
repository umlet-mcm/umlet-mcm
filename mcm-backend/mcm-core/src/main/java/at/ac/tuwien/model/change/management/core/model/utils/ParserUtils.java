package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.model.attributes.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
public class ParserUtils {
    private static final String ATTRIBUTE_VALUE_DELIM = ",";

    /**
     * Get MCM attributes from the commented out lines. Such lines start with "//" and each line
     * contains a "key: value" pair. Multiple values are parsed into lists, floats and ints are
     * converted automatically. Reserved attributes defined in {@link McmKeyDefinitions} are verified.
     *
     * @param text The text that contains the commented out attributes.
     * @return Attributes as key-value pairs.
     * @throws McmAttributesException
     */
    public static HashMap<String, Object> extractAttributesFromComments(String text, boolean ignoreKeyDefinitionChecks) throws McmAttributesException {
        HashMap<String, Object> attrs = new LinkedHashMap<>();
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

        if (ignoreKeyDefinitionChecks) {
            return attrs;
        }

        // verify key definitions
        Field[] mcmFields = McmKeyDefinitions.class.getDeclaredFields();
        for (var field : mcmFields) {
            field.setAccessible(true);
            try {
                String key = (String) field.get(McmKeyDefinitions.class);

                // check if required attribute is present
                if (field.isAnnotationPresent(Required.class)) {
                    if (!attrs.containsKey(key)) {
                        throw new McmAttributesException(
                                "Required attribute '" + key + "' missing."
                        );
                    }
                }

                if (!attrs.containsKey(key)) {
                    continue;
                }

                // verify type
                Object value = attrs.get(key);
                if (field.isAnnotationPresent(HasType.class)) {
                    HasType annotation = field.getAnnotation(HasType.class);
                    Class<?> target = annotation.type();

                    // for a list test every element
                    if (field.isAnnotationPresent(Listable.class)) {
                        @SuppressWarnings("unchecked") // the field was annotated so it must be a list
                        List<Object> vals = (List<Object>) value;
                        for (Object o : vals) {
                            verifyHasType(key, o, target);
                        }
                    } else {
                        verifyHasType(key, value, target);
                    }
                }
            } catch (IllegalAccessException e) {
                // should only occur if a field in McmKeyDefinitions was declared private for some reason
                throw new RuntimeException(e.getMessage());
            }

        }

        return attrs;
    }

    private static void verifyHasType(String key, Object value, Class<?> target) throws McmAttributesException {
        if (value.getClass() != target) {
            throw new McmAttributesException(
                    "Attribute '" + key + "' must have type '" + target + "' but '" + value.getClass() + "' found instead."
            );
        }
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
            res[1] = Arrays.stream(values).map(v -> ParserUtils.tryParseString(v.trim(), false)).toList();
        } else {
            // if a reserved attribute is Listable then it should always be stored in a list
            // even if it only has one element
            boolean wrapInList = isListable((String) res[0]);
            res[1] = ParserUtils.tryParseString(kv[1].trim(), wrapInList);
        }

        return res;
    }

    /**
     * Check if a key has a Listable annotation in the {@link McmKeyDefinitions}.
     *
     * @param key The key to be checked.
     * @return True if the key is Listable, false if it isn't or is not present in the definitions.
     */
    private static boolean isListable(String key) {
        Field[] mcmFields = McmKeyDefinitions.class.getDeclaredFields();
        for (var field : mcmFields) {
            field.setAccessible(true);
            try {
                String mcmKey = (String) field.get(McmKeyDefinitions.class);
                if (mcmKey.equals(key) && field.isAnnotationPresent(Listable.class)) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                // should only occur if a field in McmKeyDefinitions was declared private for some reason
                throw new RuntimeException(e.getMessage());
            }
        }
        return false;
    }

    /**
     * Try to parse the input into an int, float or string.
     *
     * @param in         The string to be parsed.
     * @param wrapInList If set to true the result will be returned as list.
     * @return The result as int or float for if the value of the input was numerical. If the input was a simple
     * string it will be returned with the quotes removed. Inputs not belonging to any category are kept as strings
     * and not modified.
     */
    public static Object tryParseString(String in, boolean wrapInList) {
        if (in.startsWith("\"") && in.endsWith("\"") && in.length() > 1) {
            // the value was originally a string, remove the leading and trailing "
            String res = in.substring(1, in.length() - 1);
            return wrapInList ? new ArrayList<>(List.of(res)) : res;
        }

        try {
            return wrapInList ? new ArrayList<>(List.of(Integer.parseInt(in))) : Integer.parseInt(in);
        } catch (NumberFormatException ignored) {
        }

        try {
            return wrapInList ? new ArrayList<>(List.of(Float.parseFloat(in))) : Float.parseFloat(in);
        } catch (NumberFormatException ignored) {
        }

        return wrapInList ? new ArrayList<>(List.of(in)) : in;
    }
}
