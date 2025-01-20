package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.git.annotation.GitComponent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@GitComponent
public class VersionNameGeneratorImpl implements VersionNameGenerator {
    private static final String VERSION_NAME_PREFIX = "v";
    private static final String VERSION_NAME_SUFFIX = "";
    private static final String VERSION_NAME_SEPARATOR = ".";

    // allows versions like v1.0.0, v2.0.0, v3.0.0, ...
    // multiple digits are allowed in the MAJOR version i.e. v10.0.0, v100.0.0, ..., so long as it doesn't start with 0
    // MINOR and PATCH versions are single digits
    private static final String VERSION_NAME_PATTERN = "^v[1-9][0-9]*\\.[0-9]\\.[0-9]$";
    private static final int VERSION_INCREMENT = 1;

    @Override
    public String generateInitialVersionName() {
        return generateVersionNameFromInteger(100)
                .orElseThrow(() -> new IllegalStateException("Failed to generate initial version name"));
    }

    @Override
    public Optional<String> incrementVersionName(@NonNull String versionName) {
        return parseVersionFromStr(versionName)
                .flatMap(version -> generateVersionNameFromInteger(version + VERSION_INCREMENT));

    }

    @Override
    public Optional<String> findMostRecentVersionName(@NonNull Collection<String> names) {
        return names.stream().map(this::parseVersionFromStr)
                .flatMap(Optional::stream)
                .max(Integer::compareTo)
                .flatMap(this::generateVersionNameFromInteger);
    }

    @Override
    public boolean isAutoGeneratedVersionName(@NonNull String versionName) {
        return Pattern.matches(VERSION_NAME_PATTERN, versionName);
    }

    private Optional<String> generateVersionNameFromInteger(int version) {
        String versionStr = Integer.toString(version);
        int length = versionStr.length();

        if (length < 3) return Optional.empty();

        StringBuilder versionName = new StringBuilder(VERSION_NAME_PREFIX);

        if (length == 3) {
            versionName.append(String.join(VERSION_NAME_SEPARATOR, versionStr.split("")));
        } else {
            versionName.append(versionStr, 0, length - 2)
                    .append(VERSION_NAME_SEPARATOR)
                    .append(String.join(VERSION_NAME_SEPARATOR, versionStr.substring(length - 2).split("")));
        }

        versionName.append(VERSION_NAME_SUFFIX);
        return Optional.of(versionName.toString());
    }

    private Optional<Integer> parseVersionFromStr(String versionName) {
        if (!isAutoGeneratedVersionName(versionName)) return Optional.empty();
        try {
            var versionStr = versionName.substring(VERSION_NAME_PREFIX.length(), versionName.length() - VERSION_NAME_SUFFIX.length());
            return Optional.of(Integer.parseInt(versionStr.replace(VERSION_NAME_SEPARATOR, "")));
        } catch (NumberFormatException e) {
            log.debug("Failed to parse version from String '{}'", versionName);
            return Optional.empty();
        }
    }

}
