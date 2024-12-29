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
        if (description != null && !description.endsWith("\n")) {
            sb.append("\n");
        }

        // add attributes
        if (mcmAttributes != null) {
            for (var kv : mcmAttributes.entrySet()) {
                if (kv.getValue() instanceof List<?>) {
                    sb.append("// " + kv.getKey() + ": ");
                    if (((List<?>) kv.getValue()).size() > 0) {
                        for (Object v : (List<?>) kv.getValue()) {
                            if (v instanceof String) { // strings should have enclosing "s when exported to uxf
                                sb.append("\"" + v + "\"");
                            } else {
                                sb.append(v);
                            }

                            sb.append(", ");
                        }
                        sb.delete(sb.length() - 2, sb.length()); // remove trailing ", "
                    }
                    sb.append("\n");
                } else {
                    if (kv.getValue() == null) {
                        continue;
                    }

                    String newVal = kv.getValue().toString();
                    if (kv.getValue() instanceof String) {
                        newVal = "\"" + kv.getValue() + "\""; // strings should have enclosing "s when exported to uxf
                    }
                    sb.append("// " + kv.getKey() + ": " + newVal + "\n");
                }
            }
        }
        return sb.toString();
    }
}
