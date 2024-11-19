package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
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
    protected String description;
    protected Map<String, Object> mcmAttributes;
    private String mcmType = null;

    @Nullable
    public String getMcmId() {
        return (String) mcmAttributes.get(AttributeKeys.ID);
    }

    @Nullable
    public String getMcmType() {
        if (mcmType != null) {
            return mcmType;
        }

        // find the type key in the attributes
        for (var a : mcmAttributes.entrySet()) {
            // for compatibility reasons, older uxf files use keys like "PPR type", "PPR UVL type"
            if (a.getKey().toLowerCase().contains("type")) {
                mcmType = (String) a.getValue();
            }
        }
        return mcmType;
    }
}
