package at.ac.tuwien.model.change.management.core.utils;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationValidationException;
import lombok.NonNull;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class ConfigurationUtils {
    private ConfigurationUtils() {
    }

    private final static class Patterns {
        private static final Pattern PATHNAME_SEPARATOR = Pattern.compile("[/\\\\]");
        private static final Pattern PATHNAME_INVALID_CHARS = Pattern.compile("[^\\p{L}\\p{N} ._-]");
        private static final Pattern PATHNAME_STRIP_CHARS = Pattern.compile("^[.-]+|[.-]+$");
        private static final Pattern PATHNAME_RESERVED = Pattern.compile("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$", Pattern.CASE_INSENSITIVE);
        private static final int PATHNAME_MAX_LENGTH = 127; // Adjust as necessary
    }

    public static String sanitizeConfigurationName(@NonNull String name) {
        if (name.isBlank()) {
            throw new ConfigurationValidationException("Configuration name must not be blank");
        }

        var sanitizedName = Normalizer.normalize(name.strip(), Normalizer.Form.NFKC);
        sanitizedName = Patterns.PATHNAME_SEPARATOR.matcher(sanitizedName).replaceAll("-");
        sanitizedName = Patterns.PATHNAME_INVALID_CHARS.matcher(sanitizedName).replaceAll("");
        sanitizedName = Patterns.PATHNAME_STRIP_CHARS.matcher(sanitizedName).replaceAll("");

        if (Patterns.PATHNAME_RESERVED.matcher(sanitizedName).matches()) {
            sanitizedName = "_" + sanitizedName;
        }

        if (sanitizedName.length() > Patterns.PATHNAME_MAX_LENGTH) {
            throw new ConfigurationValidationException("Directory name '" + sanitizedName + "' cannot be longer than " +
                    Patterns.PATHNAME_MAX_LENGTH + " characters");
        }

        if (sanitizedName.isBlank()) {
            throw new ConfigurationValidationException("Directory name '" + sanitizedName + "' contains no valid characters");
        }

        return sanitizedName;
    }
}
