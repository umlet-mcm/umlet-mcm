package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.Set;

public interface ConfigurationDSLTransformer {

    /**
     * Parses the given models, nodes and relations to a configuration.
     * Relies on {@link at.ac.tuwien.model.change.management.core.transformer.DSLTransformer}
     * @param models the String / DSL representation of the models to parse
     * @param nodes the String / DSL representation of the nodes to parse
     * @param relations the String / DSL representation of the relations to parse
     * @param configurationName the name of the configuration
     * @param configurationVersion the version of the configuration
     * @return the parsed configuration objectj
     */
    Configuration parseToConfiguration(
            @NonNull Set<String> models,
            @NonNull Set<String> nodes,
            @NonNull Set<String> relations,
            @Nullable String configurationName,
            @Nullable String configurationVersion
    );

    /**
     * Serializes the given configuration to its DSL representation.
     * Relies on {@link at.ac.tuwien.model.change.management.core.transformer.DSLTransformer}
     * @param configuration the configuration to serialize
     * @return the DSL representation of the configuration
     */
    ConfigurationContents<DSLElement<Model>, DSLElement<Node>, DSLElement<Relation>> serializeToDsl(@NonNull Configuration configuration);

    /**
     * Parses the given configuration DSL to a configuration object.
     * NOTE that with this implementation, configuration name and version are not set.
     * You may have to set them manually after calling this or use {@link #parseToConfiguration(ConfigurationContents, String, String)}
     * @param configurationDSL a {@link ConfigurationContents} object containing the String / DSL representations
     *                         of the models, nodes and relations to parse
     * @return the parsed configuration object
     */
    default Configuration parseToConfiguration(@NonNull ConfigurationContents<String, String, String> configurationDSL) {
        return parseToConfiguration(configurationDSL, null, null);
    }

    /**
     * Parses the given configuration DSL to a configuration object.
     * @param configurationDSL a {@link ConfigurationContents} object containing the String / DSL representations
     *                         of the models, nodes and relations to parse
     * @param configurationName the name of the configuration
     * @param configurationVersion the version of the configuration
     * @return the parsed configuration object
     */
    default Configuration parseToConfiguration(
            @NonNull ConfigurationContents<String, String, String> configurationDSL,
            @Nullable String configurationName,
            @Nullable String configurationVersion
    ) {
        return parseToConfiguration(
                configurationDSL.getModels(),
                configurationDSL.getNodes(),
                configurationDSL.getRelations(),
                configurationName,
                configurationVersion
        );
    }

    /**
     * Parses the given models, nodes and relations to a configuration.
     * NOTE that with this implementation, configuration name and version are not set.
     * You may have to set them manually after calling this or use {@link #parseToConfiguration(Set, Set, Set, String, String)}
     * @param models the String / DSL representation of the models to parse
     * @param nodes the String / DSL representation of the nodes to parse
     * @param relations the String / DSL representation of the relations to parse
     * @return the parsed configuration object
     */
    default Configuration parseToConfiguration(
            @NonNull Set<String> models,
            @NonNull Set<String> nodes,
            @NonNull Set<String> relations
    ) {
        return parseToConfiguration(models, nodes, relations, null, null);
    }
}
