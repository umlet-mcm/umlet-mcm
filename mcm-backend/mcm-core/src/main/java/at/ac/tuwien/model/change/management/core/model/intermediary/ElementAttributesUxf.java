package at.ac.tuwien.model.change.management.core.model.intermediary;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Attributes specific to nodes, relations etc.
 */
@Getter
@Setter

public class ElementAttributesUxf extends BaseAttributesUxf {
    @Nullable
    private Map<String, String> umletAttributes;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());

        if (umletAttributes != null) {
            for (var kv : umletAttributes.entrySet()) {
                sb.append(kv.getKey() + "=" + kv.getValue() + "\n");
            }
        }
        return sb.toString();
    }
}

