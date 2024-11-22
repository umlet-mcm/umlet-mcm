package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Abstract base class for attributes that are present in all elements, including the model itself.
 */
@Getter
@Setter
public abstract class BaseAttributes {
    @Nullable
    protected String id;
    protected String description;
    protected Map<String, Object> mcmAttributes;

    // Finding the type of an element requires a custom logic so a custom getter is implemented.
    @Nullable
    public String getMcmType() {
        // find the type key in the attributes
        for (var a : mcmAttributes.entrySet()) {
            // for compatibility reasons, older uxf files use keys like "PPR type", "PPR UVL type"
            if (a.getKey().toLowerCase().contains("type")) {
                return (String) a.getValue();
            }
        }
        return null;
    }

}
