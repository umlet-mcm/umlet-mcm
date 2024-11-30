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
    protected String mcmModel;

    /**
     * key=value attributes extracted from panel_attributes. E.g. background color
     */
    protected LinkedHashMap<String, String> umletAttributes;

    @Nullable
    protected String pprType;
}
