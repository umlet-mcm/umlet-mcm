package at.ac.tuwien.model.change.management.core.model.attributes;

import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Abstract base class for attributes that are present in all elements, including the model itself.
 */
@Getter
@Setter
public abstract class BaseAttributes {
    protected String id;

    protected List<String> tags;

    /**
     * The beginning of the panel_attributes (or help_text) until the first horizontal line ("--").
     * Comments and Umlet attributes are removed.
     */
    protected String title;

    /**
     * All text from panel_attributes (or help_text) that is not commented and not
     * Umlet attribute definitions (key=value).
     */
    protected String description;

    /**
     * Custom, non-Umlet related and non-reserved attributes defined in the comments.
     */
    protected LinkedHashMap<String, Object> mcmAttributes;

    /**
     * For each MCM attribute key store the corresponding inline comment or null
     * if there's no comment
     */
    protected LinkedHashMap<String, String> mcmAttributesInlineComments;

    public void updateDescriptionAndTitle(String descriptionWithTitle) {
        this.description = ParserUtils.getDescription(descriptionWithTitle);
        this.title = ParserUtils.getTitle(descriptionWithTitle);
    }
}
