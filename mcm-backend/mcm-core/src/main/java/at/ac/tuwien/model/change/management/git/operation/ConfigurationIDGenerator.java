package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import lombok.NonNull;

public interface ConfigurationIDGenerator {

    /**
     * Sets the ID of the given configuration element (Model, Node or Relation).
     * @param configurationElement the configuration element for which to set the ID
     * @param allowOverwrite whether to allow overwriting an existing ID
     *                       Note that if allowOverwrite is false and the configuration element already has an ID,
     *                       we throw an exception.
     * @return the ID that was generated and assigned to the configuration element
     */
    String setID(@NonNull BaseAttributes configurationElement, boolean allowOverwrite);

    /**
     * Generates a new ID.
     * @return the generated ID
     */
    String generateID();

    /**
     * Sets the ID of the given configuration element (Model, Node or Relation).
     * @param configurationElement the configuration element for which to set the ID
     *                             Note that if the configuration element already has an ID, we throw an exception.
     * @return the ID that was generated and assigned to the configuration element
     */
    default String setID(@NonNull BaseAttributes configurationElement) {
        return setID(configurationElement, false);
    }
}
