package at.ac.tuwien.model.change.management.server.testutil;

import at.ac.tuwien.model.change.management.server.dto.ConfigurationDTO;
import lombok.NonNull;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

public class ConfigurationDTOAssert extends AbstractObjectAssert<ConfigurationDTOAssert, ConfigurationDTO> {

    public ConfigurationDTOAssert(@NonNull ConfigurationDTO actual) {
        super(actual, ConfigurationDTOAssert.class);
    }

    public static ConfigurationDTOAssert assertThat(@NonNull ConfigurationDTO actual) {
        return new ConfigurationDTOAssert(actual);
    }

    public ConfigurationDTOAssert containsSameElementsAs(@NonNull ConfigurationDTO expected) {
        isNotNull();
        Assertions.assertThat(actual).usingRecursiveComparison(recursiveFieldsComparison()).isEqualTo(expected);
        return this;
    }

    public ConfigurationDTOAssert hasName(@NonNull String otherName) {
        isNotNull();
        Assertions.assertThat(actual.name()).isEqualTo(otherName);
        return this;
    }

    public ConfigurationDTOAssert hasVersion(@NonNull String otherVersion) {
        isNotNull();
        Assertions.assertThat(actual.version()).isEqualTo(otherVersion);
        return this;
    }

    public ConfigurationDTOAssert hasVersionName(@NonNull String versionName) {
        isNotNull();
        Assertions.assertThat(actual.version().name()).isEqualTo(versionName);
        return this;
    }

    public ConfigurationDTOAssert hasVersionCustomName(@NonNull String versionCustomName) {
        isNotNull();
        Assertions.assertThat(actual.version().customName()).isEqualTo(versionCustomName);
        return this;
    }

    public ConfigurationDTOAssert hasValidVersion() {
        isNotNull();
        Assertions.assertThat(actual.version())
                .isNotNull();
        return this;
    }

    public AbstractStringAssert<?> name() {
        isNotNull();
        return this.extracting(ConfigurationDTO::name).asInstanceOf(InstanceOfAssertFactories.STRING);
    }

    public AbstractStringAssert<?> version() {
        isNotNull();
        return this.extracting(ConfigurationDTO::version).asInstanceOf(InstanceOfAssertFactories.STRING);
    }

    private static RecursiveComparisonConfiguration recursiveFieldsComparison() {
        var recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.ignoreFields(
                "name",
                "version",
                "models.id",
                "models.nodes.id",
                "models.nodes.mcmModelId",
                "models.nodes.relations.id",
                "models.nodes.relations.mdmModelId"
        );
        return recursiveComparisonConfiguration;
    }
}
