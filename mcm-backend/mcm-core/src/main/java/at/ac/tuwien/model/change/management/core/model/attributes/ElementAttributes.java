package at.ac.tuwien.model.change.management.core.model.attributes;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Attributes specific to nodes, relations etc.
 */
@Getter
@Setter
public class ElementAttributes extends BaseAttributes {
    @Nullable
    private Map<String, String> umletAttributes;
}

