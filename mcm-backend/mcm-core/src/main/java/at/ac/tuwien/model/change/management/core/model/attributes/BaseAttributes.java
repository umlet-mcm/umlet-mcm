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
     * The full content of panel_attributes for elements or help_text for a model.
     */
    protected String originalText;

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

    public void updateDescriptionAndTitle(String descriptionWithTitle) {
        this.description = ParserUtils.getDescription(descriptionWithTitle);
        this.title = ParserUtils.getTitle(descriptionWithTitle);
    }
}
