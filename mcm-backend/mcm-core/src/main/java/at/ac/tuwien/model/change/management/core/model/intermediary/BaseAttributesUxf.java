package at.ac.tuwien.model.change.management.core.model.intermediary;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
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

    private String mcmType;

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
