package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.InvalidNameException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NameValidationServiceImpl implements NameValidationService {

    private static class Path {
        private static final int MAX_LENGTH = 127; // Adjust as necessary
        private static final Pattern SEPARATOR_CHARS = Pattern.compile("[/\\\\]");
        private static final Pattern INVALID_CHARS = Pattern.compile("[^\\p{L}\\p{N} ._-]");
        private static final Pattern INVALID_ENDINGS = Pattern.compile("^[.-]+|[.-]+$");
        private static final Pattern RESERVED = Pattern.compile("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$", Pattern.CASE_INSENSITIVE);
    }

    private static class Version {
        private static final Map<Character, String> ENCODING = Map.ofEntries(
                Map.entry(' ', "%20"),
                Map.entry('\\', "%5C"),
                Map.entry('?', "%3F"),
                Map.entry('~', "%7E"),
                Map.entry('^', "%5E"),
                Map.entry(':', "%3A"),
                Map.entry('*', "%2A"),
                Map.entry('[', "%5B"),
                Map.entry('@', "%40"),
                Map.entry('/', "%2F")
        );

        private static final Map<String, Character> DECODING = ENCODING.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getValue,
                Map.Entry::getKey
        ));

        private static final Pattern CONSECUTIVE_DASHES = Pattern.compile("-{2,}");
        private static final Pattern CONSECUTIVE_DOTS = Pattern.compile("\\.{2,}");
        private static final Pattern INVALID_ENDINGS = Pattern.compile("^[.-]+|[.-]+$");
        private static final int ENCODED_CHAR_LENGTH = 3;
    }

    @Override
    public void validateRepositoryName(@NonNull String name) throws InvalidNameException {
        log.debug("Checking validity of repository name '{}'", name);
        if (name.isBlank()) {
            throw new InvalidNameException("Repository name must not be blank");
        }
        if (name.length() > Path.MAX_LENGTH) {
            throw new InvalidNameException("Repository name cannot be longer than " + Path.MAX_LENGTH + " characters");
        }
        if (Path.SEPARATOR_CHARS.matcher(name).find()) {
            throw new InvalidNameException("Repository name cannot contain path separators such as '/' or '\\'");
        }
        if (Path.INVALID_CHARS.matcher(name).find()) {
            throw new InvalidNameException("Repository name contains invalid characters");
        }
        if (Path.INVALID_ENDINGS.matcher(name).find()) {
            throw new InvalidNameException("Repository name must not start or end with hyphens or periods");
        }
        if (Path.RESERVED.matcher(name).find()) {
            throw new InvalidNameException("Repository name contains keyword possibly reserved by the operating system");
        }
        log.debug("Repository name '{}' considered valid", name);
    }

    @Override
    public String encodeVersionName(@NonNull String name, boolean sanitize) {
        log.debug("Encoding version name '{}'", name);
        StringBuilder encoder = new StringBuilder();
        for (char c : name.toCharArray()) {
            String encoded = Version.ENCODING.get(c);
            encoder.append(encoded == null ? c : encoded);
        }
        var basicEncoded = encoder.toString();
        var finalEncoded = sanitize ? sanitizeVersionName(basicEncoded) : basicEncoded;
        log.debug("Encoded version name '{}' as '{}'", name, finalEncoded);
        return finalEncoded;
    }

    @Override
    public String decodeVersionName(@NonNull String name) {
        log.debug("Decoding version name '{}'", name);
        StringBuilder decoder = new StringBuilder();
        for(int i = 0; i < name.length(); ++i) {
            var currentChar = name.charAt(i);
            if (currentChar == '%' && i + (Version.ENCODED_CHAR_LENGTH - 1) < name.length()) {
                var encodedSubstring = name.substring(i, i + Version.ENCODED_CHAR_LENGTH);
                var decodedChar = Version.DECODING.get(encodedSubstring);
                if (decodedChar != null) {
                    decoder.append(decodedChar);
                    i += (Version.ENCODED_CHAR_LENGTH - 1);
                } else {
                    decoder.append(currentChar);
                }
            } else {
                decoder.append(currentChar);
            }
        }
        var decodedName = decoder.toString();
        log.debug("Decoded version name '{}' as '{}'", name, decodedName);
        return decodedName;
    }

    private String sanitizeVersionName(String name) {
        String sanitized = Version.CONSECUTIVE_DASHES.matcher(name).replaceAll("-");
        sanitized = Version.CONSECUTIVE_DOTS.matcher(sanitized).replaceAll(".");
        sanitized = Version.INVALID_ENDINGS.matcher(sanitized).replaceAll("");
        return sanitized;
    }
}
