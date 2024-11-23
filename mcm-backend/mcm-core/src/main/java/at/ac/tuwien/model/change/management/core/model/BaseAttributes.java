package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for attributes that are present in all elements, including the model itself.
 */
@Getter
@Setter
public abstract class BaseAttributes {
    @Nullable
    protected String id;
    /**
     * All text from panel_attributes that is not commented and not
     * Umlet related commands (e.g. draw horizontal line, set background color).
     */
    protected String description;
    @Nullable
    protected String mcmType;
    /**
     * Non-Umlet related attributes defined in the comments.
     */
    private Map<String, Object> mcmAttributes; // this is private so that the custom setter has be used

    // id and mcmType are stored in the mcmAttributes, this setter automatically populates these two fields
    public void setMcmAttributes(Map<String, Object> mcmAttributes) {
        this.mcmAttributes = mcmAttributes;
        for (var a : mcmAttributes.entrySet()) {
            // for compatibility reasons, older uxf files use keys like "PPR type", "PPR UVL type"
            if (a.getKey().toLowerCase().contains("type")) {
                // temporary fix
                // todo remove
                if(a.getValue() instanceof String){
                    this.mcmType = (String) a.getValue();
                }else{
                    this.mcmType = ((List<String>) a.getValue()).get(0);
                }
            }
        }
        this.id = (String) mcmAttributes.get(AttributeKeys.ID);
    }
}
