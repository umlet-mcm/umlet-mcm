package at.ac.tuwien.model.change.management.testutil.assertion;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import lombok.NonNull;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
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

    public ConfigurationAssert hasVersionCustomName(@NonNull String customName) {
        isNotNull();
        Assertions.assertThat(actual.getVersionCustomName()).isEqualTo(customName);
        return this;
    }

    public ConfigurationAssert hasVersionName(@NonNull String name) {
        isNotNull();
        Assertions.assertThat(actual.getVersionName()).isEqualTo(name);
        return this;
    }

    public ConfigurationAssert hasVersion(@NonNull String otherVersion) {
        isNotNull();
        Assertions.assertThat(actual.getVersionHash()).isEqualTo(otherVersion);
        return this;
    }

    public ConfigurationAssert hasValidVersion() {
        isNotNull();
        Assertions.assertThat(actual.getVersionHash())
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
        return this.extracting(Configuration::getVersionHash).asInstanceOf(InstanceOfAssertFactories.STRING);
    }

    private RecursiveComparisonConfiguration recursiveFieldsComparison() {
        var recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
        
        recursiveComparisonConfiguration.ignoreFields(
                "name",
                "version",
                "models.mcmAttributes",
                "models.tags",
                "models.umletAttributes",
                "models.mcmAttributesInlineComments",
                "models.nodes.relations.mcmAttributes",
                "models.nodes.relations.tags",
                "models.nodes.relations.mcmAttributesInlineComments",
                "models.nodes.relations.umletAttributes",
                "models.nodes.mcmAttributes",
                "models.nodes.tags",
                "models.nodes.mcmAttributesInlineComments",
                "models.nodes.umletAttributes",
                // for some reason, these Record classes trouble AssertJ's recursive comparison
                "models.nodes.relations.target",
                "models.nodes.relations.startPoint",
                "models.nodes.relations.endPoint"
        );

        return recursiveComparisonConfiguration;
    }
}
