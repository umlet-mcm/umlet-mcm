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
    @Nullable
    protected String id;
    protected String description;
    @Nullable
    protected String mcmType;
    private Map<String, Object> mcmAttributes; // this is private so that the custom setter has be used

    // id and mcmType are stored in the mcmAttributes, this setter automatically populates these two fields
    public void setMcmAttributes(Map<String, Object> mcmAttributes) {
        this.mcmAttributes = mcmAttributes;
        for (var a : mcmAttributes.entrySet()) {
            // for compatibility reasons, older uxf files use keys like "PPR type", "PPR UVL type"
            if (a.getKey().toLowerCase().contains("type")) {
                this.mcmType = (String) a.getValue();
            }
        }
    }
}
