package at.ac.tuwien.model.change.management.git.operation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class VersionNameGeneratorTest {

    private final VersionNameGenerator versionNameGenerator = new VersionNameGeneratorImpl();


    @Test
    public void testGenerateInitialVersionName_shouldBeInitialVersionName() {
        var initialVersionName = versionNameGenerator.generateInitialVersionName();
        Assertions.assertThat(initialVersionName).isEqualTo("v1.0.0");
    }

    @Test
    public void testIncrementVersionName_versionWithLeadingZero_shouldReturnEmptyOptional() {
        var versionName = "v0.0.1";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).isEmpty();
    }

    @Test
    public void testIncrementVersionName_initialVersion_shouldIncrementVersionName() {
        var versionName = "v1.0.0";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).contains("v1.0.1");
    }

    @Test
    public void testIncrementVersionName_randomLargerVersion_shouldIncrementVersionName() {
        var versionName = "v21.2.3";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).contains("v21.2.4");
    }

    @Test
    public void testIncrementVersionName_veryLargeVersion_shouldIncrementVersionName() {
        var versionName = "v9999.9.9";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).contains("v10000.0.0");
    }

    @Test
    public void testIncrementVersionName_veryVariedVersion_shouldIncrementVersionName() {
        var versionName = "v3873.2.9";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).contains("v3873.3.0");
    }

    @Test
    public void testIncrementVersionName_invalidVersionWithTwoMinorDigits_shouldReturnEmptyOptional() {
        var versionName = "v1.00.0";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).isEmpty();
    }

    @Test
    public void testIncrementVersionName_invalidVersionWithPatchDigits_shouldReturnEmptyOptional() {
        var versionName = "v1.0.10";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).isEmpty();
    }

    @Test
    public void testIncrementVersion_name_invalidVersionWithoutVersionPrefix_shouldReturnEmptyOptional() {
        var versionName = "1.0.0";
        var incrementedVersionName = versionNameGenerator.incrementVersionName(versionName);
        Assertions.assertThat(incrementedVersionName).isEmpty();
    }

    @Test
    public void testFindMostRecentVersionName_emptyCollection_shouldReturnEmptyOptional() {
        List<String> names = Collections.emptyList();
        var mostRecentVersionName = versionNameGenerator.findMostRecentVersionName(names);
        Assertions.assertThat(mostRecentVersionName).isEmpty();
    }

    @Test
    public void testFindMostRecentVersionName_singleVersion_shouldReturnVersion() {
        List<String> names = List.of("v1.0.0");
        var mostRecentVersionName = versionNameGenerator.findMostRecentVersionName(names);
        Assertions.assertThat(mostRecentVersionName).contains("v1.0.0");
    }

    @Test
    public void testFindMostRecentVersionName_multipleVersions_shouldReturnMostRecentVersion() {
        List<String> names = List.of("v1.0.0", "v1.0.1", "v1.0.2", "v1.0.3");
        var mostRecentVersionName = versionNameGenerator.findMostRecentVersionName(names);
        Assertions.assertThat(mostRecentVersionName).contains("v1.0.3");
    }

    @Test
    public void testFindMostRecentVersionName_differentVersions_shouldReturnMostRecentVersion() {
        List<String> names = List.of("v2.0.0", "v1.0.1", "v1.0.2", "v3.0.3", "v1.1.0", "v1.1.1", "v1.1.2", "v1.1.3");
        var mostRecentVersionName = versionNameGenerator.findMostRecentVersionName(names);
        Assertions.assertThat(mostRecentVersionName).contains("v3.0.3");
    }

    @Test
    public void testFindMostRecentVersionName_includingInvalidVersions_shouldReturnMostRecentValidVersion() {
        List<String> names = List.of("invalid", "v1.0.1", "v1.0.2", "v3.0.3", "v1.1.0", "v1.1.1", "v1.1.2", "v1.1.3", "v1.0.10", "v1.00.0", "v1.0.0", "v1.0.10");
        var mostRecentVersionName = versionNameGenerator.findMostRecentVersionName(names);
        Assertions.assertThat(mostRecentVersionName).contains("v3.0.3");
    }

    @Test
    public void testFindNextVersionName_emptyCollection_shouldReturnInitialVersionName() {
        List<String> names = Collections.emptyList();
        var nextVersionName = versionNameGenerator.findNextVersionName(names);
        Assertions.assertThat(nextVersionName).isEqualTo("v1.0.0");
    }

    @Test
    public void testFindNextVersionName_singleVersion_shouldReturnNextVersion() {
        List<String> names = List.of("v1.0.0");
        var nextVersionName = versionNameGenerator.findNextVersionName(names);
        Assertions.assertThat(nextVersionName).isEqualTo("v1.0.1");
    }

    @Test
    public void testFindNextVersionName_multipleVersions_shouldReturnNextVersion() {
        List<String> names = List.of("v1.0.0", "v1.0.1", "v1.0.2", "v1.0.3");
        var nextVersionName = versionNameGenerator.findNextVersionName(names);
        Assertions.assertThat(nextVersionName).isEqualTo("v1.0.4");
    }

    @Test
    public void testFindNextVersionName_differentVersions_shouldReturnNextVersion() {
        List<String> names = List.of("v2.0.0", "v1.0.1", "v1.0.2", "v3.0.3", "v1.1.0", "v1.1.1", "v1.1.2", "v1.1.3");
        var nextVersionName = versionNameGenerator.findNextVersionName(names);
        Assertions.assertThat(nextVersionName).isEqualTo("v3.0.4");
    }
}
