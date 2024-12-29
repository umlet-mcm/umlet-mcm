package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.core.model.attributes.ElementAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        if (mcmAttrs.get(AttributeKeys.TAGS) != null) {
            // convert each tag to string and make sure they are stored in a list
            var tags = mapParsedListableAttribute(mcmAttributes.get(AttributeKeys.TAGS), Object::toString);
            target.setTags(tags);
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
     * fields from {@link BaseAttributes} and stores them in a map.
     */
    public static <T extends BaseAttributes> LinkedHashMap<String, Object> mergeAttributes(T base) {
        LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        attrs.put(AttributeKeys.ID, base.getId());
        attrs.put(AttributeKeys.TAGS, base.getTags());
        return attrs;
    }

    /**
     * In the uxf representation the mcm attributes are stored in a single map. This function extracts the mcm
     * fields from {@link ElementAttributes} and it's parents and stores them in a map.
     */
    public static <T extends ElementAttributes> LinkedHashMap<String, Object> mergeAttributes(T element) {
        // merge the attributes of the base
        LinkedHashMap<String, Object> attrs = McmAttributesMapper.<BaseAttributes>mergeAttributes(element);
        attrs.put(AttributeKeys.MODEL_ID, element.getMcmModelId());
        attrs.put(AttributeKeys.MODEL, element.getMcmModel());
        attrs.put(AttributeKeys.PPR_TYPE, element.getPprType());
        return attrs;
    }

    /**
     * Ensures that the value for a listable attribute is always a list (even for a single value) and maps the value(s).
     * <p>
     * Some attributes such as "tags" can have multiple values. These value for these attributes should always be a
     * list. When the uxf is parsed attributes that have multiple values are parsed into lists, but it can happen that
     * a listable attribute only has a single value assigned. These single values have to be stored in lists. The result
     * of the parsing is always an Object or a list of Objects. These have to mapped into their respective classes.
     *
     * @param listableAttributeValue The value of an attribute that comes from the Uxf representation. It can be a list
     *                               or a single value.
     * @param callback               The mapping function that will be applied to all values.
     * @param <T>                    The target type of the mapping.
     * @return A list of the mapped value(s).
     */
    public static <T> List<T> mapParsedListableAttribute(Object listableAttributeValue, Function<Object, T> callback) {
        if (listableAttributeValue instanceof List<?>) {
            // the attribute had multiple values, apply the callback to each
            return ((List<?>) listableAttributeValue).stream()
                    .map(callback)
                    .toList();
        } else {
            // the attribute only had a single value, but it should still be stored in a list because the attribute
            // is listable
            return List.of(
                    callback.apply(listableAttributeValue)
            );
        }
    }
}
