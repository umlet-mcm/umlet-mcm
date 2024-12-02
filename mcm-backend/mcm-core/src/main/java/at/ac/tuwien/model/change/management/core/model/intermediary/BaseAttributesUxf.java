package at.ac.tuwien.model.change.management.core.model.intermediary;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Base class for attributes that are common to multiple elements in the model, such as the nodes,
 * relations and the model itself.
 */
@Getter
@Setter
public class BaseAttributesUxf {
    protected String originalText;
    protected String description;
    protected LinkedHashMap<String, Object> mcmAttributes;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);

        // add attributes
        if (mcmAttributes != null) {
            for (var kv : mcmAttributes.entrySet()) {
                if (kv.getValue() instanceof List<?>) {
                    sb.append("// " + kv.getKey() + ": ");
                    for (Object v : (List<?>) kv.getValue()) {
                        sb.append(v);
                        sb.append(", ");
                    }
                    sb.deleteCharAt(sb.length() - 1); // remove trailing ","
                    sb.append("\n");
                } else {
                    sb.append("// " + kv.getKey() + ": " + kv.getValue() + "\n");
                }
            }
        }
        return sb.toString();
    }
}
