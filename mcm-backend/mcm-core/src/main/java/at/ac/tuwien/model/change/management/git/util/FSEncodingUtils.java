package at.ac.tuwien.model.change.management.git.util;

import at.ac.tuwien.model.change.management.git.exception.IllegalNameException;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class FSEncodingUtils {

    private static final Pattern PATHNAME_SEPARATOR = Pattern.compile("[/\\\\]");
    private static final Pattern PATHNAME_INVALID_CHARS = Pattern.compile("[^\\p{L}\\p{N} ._-]");
    private static final Pattern PATHNAME_STRIP_CHARS = Pattern.compile("^[.-]+|[.-]+$");
    private static final Pattern PATHNAME_RESERVED = Pattern.compile("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$", Pattern.CASE_INSENSITIVE);
    private static final int PATHNAME_MAX_LENGTH = 127; // Adjust as necessary

    public static String escapeStringAsDirectoryName(String input) throws IllegalNameException{
        if (input == null || input.isBlank()) {
            throw new IllegalNameException("Directory name cannot be null or empty");
        }

        String sanitizedDirname = Normalizer.normalize(input, Normalizer.Form.NFKC);
        sanitizedDirname = PATHNAME_SEPARATOR.matcher(sanitizedDirname).replaceAll("-");
        sanitizedDirname = PATHNAME_INVALID_CHARS.matcher(sanitizedDirname).replaceAll("");
        sanitizedDirname = PATHNAME_STRIP_CHARS.matcher(sanitizedDirname).replaceAll("");
        sanitizedDirname = sanitizedDirname.strip();
        if (PATHNAME_RESERVED.matcher(sanitizedDirname).matches()) {
            sanitizedDirname = "_" + sanitizedDirname;
        }


        if (sanitizedDirname.length() > PATHNAME_MAX_LENGTH) {
            throw new IllegalNameException("Directory name '" + input + "' cannot be longer than " + PATHNAME_MAX_LENGTH + " characters");
        }

        if (sanitizedDirname.isBlank()) {
            throw new IllegalNameException("Directory name '" + input + "' contains no valid characters");
        }

        return sanitizedDirname;
    }
}
