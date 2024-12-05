package at.ac.tuwien.model.change.management.core.model.attributes;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

/**
 * Attributes that are common to all elements
 */
@Getter
@Setter
public abstract class ElementAttributes extends BaseAttributes {

    /**
     * The type of the model e.g. PAN, CEN
     */
    protected String mcmModel;

    /**
     * The UUID of the model to which the element belongs to
     */
    protected String mcmModelId;

    /**
     * key=value attributes extracted from panel_attributes. E.g. background color
     */
    protected LinkedHashMap<String, String> umletAttributes;

    @Nullable
    protected String pprType;
}
