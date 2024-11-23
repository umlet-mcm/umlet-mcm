package at.ac.tuwien.model.change.management.core.model.intermediary;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Base class for attributes that are common to multiple elements in the model, such as the nodes,
 * relations and the model itself.
 */
@Getter
@Setter
public class BaseAttributesUxf {
    protected String description;
    protected Map<String, Object> mcmAttributes;
}
