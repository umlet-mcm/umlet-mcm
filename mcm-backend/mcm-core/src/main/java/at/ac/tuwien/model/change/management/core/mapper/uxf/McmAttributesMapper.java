package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.core.model.attributes.ElementAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class McmAttributesMapper {

    /**
     * The uxf representation holds mcm attributes in a single map, but the model classes store
     * the reserved attributes in separate fields. This function extracts those attributes and
     * stores them in the corresponding fields. This function only extracts the fields present
     * in {@link BaseAttributes}.
     */
    public static <T extends BaseAttributes> T populateFields(Map<String, Object> mcmAttributes, T target) {
        LinkedHashMap<String, Object> mcmAttrs = new LinkedHashMap<>(mcmAttributes);

        target.setId((String) mcmAttrs.get(AttributeKeys.ID));
        mcmAttrs.remove(AttributeKeys.ID); // only store these values once
        target.setTags(new ArrayList<>());
        if (mcmAttrs.get(AttributeKeys.TAGS) != null) {
            target.getTags().addAll((List<String>) mcmAttrs.get(AttributeKeys.TAGS));
        }
        mcmAttrs.remove(AttributeKeys.TAGS);

        target.setMcmAttributes(mcmAttrs);
        return target;
    }

    /**
     * The uxf representation holds mcm attributes in a single map, but the model classes store
     * the reserved attributes in separate fields. This function extracts those attributes and
     * stores them in the corresponding fields. This function only extracts the fields present
     * in {@link ElementAttributes} and it's parents.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ElementAttributes> T populateFields(Map<String, Object> mcmAttributes, T target) {
        // use the other method to populate the common fields
        ElementAttributes populatedBase = (ElementAttributes) McmAttributesMapper.<BaseAttributes>populateFields(mcmAttributes, target);

        populatedBase.setMcmModelId((String) populatedBase.getMcmAttributes().get(AttributeKeys.MODEL_ID));
        populatedBase.getMcmAttributes().remove(AttributeKeys.MODEL_ID);
        populatedBase.setMcmModel((String) populatedBase.getMcmAttributes().get(AttributeKeys.MODEL));
        populatedBase.getMcmAttributes().remove(AttributeKeys.MODEL);
        populatedBase.setPprType((String) populatedBase.getMcmAttributes().get(AttributeKeys.PPR_TYPE));
        populatedBase.getMcmAttributes().remove(AttributeKeys.PPR_TYPE);

        // T is a subclass of ElementAttributes, this cast shouldn't be a problem
        return (T) populatedBase;
    }

    /**
     * In the uxf representation the mcm attributes are stored in a single map. This function extracts the mcm
     * fields from {@link BaseAttributes} and stores them in a list.
     */
    public static <T extends BaseAttributes> LinkedHashMap<String, Object> mergeAttributes(T base) {
        LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (base.getMcmAttributes() == null) {
            return attrs;
        }
        attrs.put(AttributeKeys.ID, base.getId());
        attrs.put(AttributeKeys.TAGS, base.getTags());
        return attrs;
    }

    /**
     * In the uxf representation the mcm attributes are stored in a single map. This function extracts the mcm
     * fields from {@link ElementAttributes} and it's parents and stores them in a list.
     */
    public static <T extends ElementAttributes> LinkedHashMap<String, Object> mergeAttributes(T element) {
        // merge the attributes of the base
        LinkedHashMap<String, Object> attrs = McmAttributesMapper.<BaseAttributes>mergeAttributes(element);
        if (element.getMcmAttributes() == null) {
            return attrs;
        }
        attrs.put(AttributeKeys.MODEL_ID, element.getMcmModelId());
        attrs.put(AttributeKeys.MODEL, element.getMcmModel());
        attrs.put(AttributeKeys.PPR_TYPE, element.getPprType());
        return attrs;
    }
}
