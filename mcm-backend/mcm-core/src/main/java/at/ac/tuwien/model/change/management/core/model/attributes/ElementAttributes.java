package at.ac.tuwien.model.change.management.core.model.attributes;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * Attributes that are common to all elements
 */
@Getter
@Setter
public abstract class ElementAttributes extends BaseAttributes{
    protected String mcmModel;

    @Nullable
    protected String pprType;
}
