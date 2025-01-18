package at.ac.tuwien.model.change.management.testutil.assertion;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import lombok.NonNull;
import org.assertj.core.api.*;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

public class ConfigurationAssert extends AbstractObjectAssert<ConfigurationAssert, Configuration> {

    public ConfigurationAssert(@NonNull Configuration actual) {
        super(actual, ConfigurationAssert.class);
    }

    public static ConfigurationAssert assertThat(@NonNull Configuration actual) {
        return new ConfigurationAssert(actual);
    }

    public ConfigurationAssert containsSameElementsAs(@NonNull Configuration expected) {
        isNotNull();
        Assertions.assertThat(actual).usingRecursiveComparison(recursiveFieldsComparison()).isEqualTo(expected);
        return this;
    }

    public ConfigurationAssert hasName(@NonNull String otherName) {
        isNotNull();
        Assertions.assertThat(actual.getName()).isEqualTo(otherName);
        return this;
    }

    public ConfigurationAssert hasVersion(@NonNull String otherVersion) {
        isNotNull();
        Assertions.assertThat(actual.getVersion()).isEqualTo(otherVersion);
        return this;
    }

    public ConfigurationAssert hasValidVersion() {
        isNotNull();
        Assertions.assertThat(actual.getVersion())
                .isNotNull()
                .isNotBlank()
                .hasSize(40);
        return this;
    }

    public AbstractStringAssert<?> name() {
        isNotNull();
        return this.extracting(Configuration::getName).asInstanceOf(InstanceOfAssertFactories.STRING);
    }

    public AbstractStringAssert<?> version() {
        isNotNull();
        return this.extracting(Configuration::getVersion).asInstanceOf(InstanceOfAssertFactories.STRING);
    }

    private static RecursiveComparisonConfiguration recursiveFieldsComparison() {
        var recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.ignoreFields(
                "name",
                "version",

                // for some reason, these Record classes trouble AssertJ's recursive comparison
                "models.nodes.relations.target",
                "models.nodes.relations.startPoint",
                "models.nodes.relations.endPoint"
        );
        return recursiveComparisonConfiguration;
    }
}
