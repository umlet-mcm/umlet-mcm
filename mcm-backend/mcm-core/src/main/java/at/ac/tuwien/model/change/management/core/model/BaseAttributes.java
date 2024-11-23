package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Abstract base class for attributes that are present in all elements, including the model itself.
 */
@Getter
@Setter
public abstract class BaseAttributes {
    /**
     * All text from panel_attributes that is not commented and not
     * Umlet related commands (e.g. draw horizontal line, set background color).
     */
    protected String description;

    /**
     * Non-Umlet related attributes defined in the comments.
     */
    protected Map<String, Object> mcmAttributes;
}
