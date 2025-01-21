package at.ac.tuwien.model.change.management.core.model.utils;

import at.ac.tuwien.model.change.management.core.mapper.uxf.McmAttributesMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationUtils {
    // the merged attributes have a different format than the normal mcm attributes
    // so we use a different pattern to extract them
    // format: // __modelId_attributeName: value
    public static Pattern MERGED_MODEL_ATTRIBUTE_PATTERN = Pattern.compile("^\\/\\/\\s*__([a-zA-Z0-9][a-zA-Z0-9-]*)_([a-zA-Z][a-zA-Z0-9]*)\\s*:(.*)");
    public static Pattern MERGED_MODEL_ATTRIBUTE_PATTERN_WITH_INLINE_COMMENT = Pattern.compile("^\\/\\/\\s*__([a-zA-Z0-9][a-zA-Z0-9-]*)_([a-zA-Z][a-zA-Z0-9]*)\\s*:(.*)(\\/\\/.*)");

    public static Configuration processImportedConfiguration(Model importedConfiguration) {
        Configuration result = new Configuration();
        result.setName((String) importedConfiguration.getMcmAttributes().get(AttributeKeys.CONFIGURATION_ID));

        // create models based on the node model IDs

        Map<String, Map<String, Pair<Object, String>>> modelAttributes = extractMergedModelAttributes(importedConfiguration);

        // if a node has no model ID it will be stored here
        Model unversionedModel = new Model();
        unversionedModel.setNodes(new HashSet<>());
        unversionedModel.setZoomLevel(importedConfiguration.getZoomLevel());

        Map<String, Model> modelMap = new HashMap<>();
        for (Node n : importedConfiguration.getNodes()) {
            if (n.getMcmModelId() == null) {
                unversionedModel.getNodes().add(n);
            }

            // if the node has an ID add it to the model map
            else {
                Model model = modelMap.get(n.getMcmModelId());
                if (model == null) {
                    model = new Model();
                    model.setNodes(new HashSet<>());
                    model.setZoomLevel(10);
                    // add the metadata to the model
                    if (modelAttributes.get(n.getMcmModelId()) != null) {
                        // split into attributes and inline comments
                        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
                        LinkedHashMap<String, String> inlineComments = new LinkedHashMap<>();
                        var attributesAndInlineComments = modelAttributes.get(n.getMcmModelId());
                        for (var kv : attributesAndInlineComments.entrySet()) {
                            attributes.put(kv.getKey(), kv.getValue().getLeft());
                            if(kv.getValue().getRight() != null){
                                inlineComments.put(kv.getKey(), kv.getValue().getRight());
                            }
                        }
                        McmAttributesMapper.populateModelFields(attributes, model);
                        model.setMcmAttributesInlineComments(inlineComments);
                    }
                    modelMap.put(n.getMcmModelId(), model);
                }
                model.getNodes().add(n);
            }
        }

        if (!unversionedModel.getNodes().isEmpty()) {
            result.getModels().add(unversionedModel);
        }
        for (Model m : modelMap.values()) {
            result.getModels().add(m);
        }

        return result;
    }

    /**
     * Extract the merged model attributes from the imported configuration
     *
     * @param model the imported configuration as a model
     * @return a map that contains the model ID as key and a map of pairs of attributes and
     * the corresponding inline comments as value
     */
    public static Map<String, Map<String, Pair<Object, String>>> extractMergedModelAttributes(Model model) {
        Map<String, Map<String, Pair<Object, String>>> result = new LinkedHashMap<>();
        for (var kv : model.getMcmAttributes().entrySet()) {
            if (!kv.getKey().startsWith("__")) {
                continue;
            }

            String line = "// " + kv.getKey() + ": " + kv.getValue();

            Matcher matcher = MERGED_MODEL_ATTRIBUTE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            String modelId = matcher.group(1);
            Object[] mcmKv = ParserUtils.getMcmKeyValueInlineComment("// " + matcher.group(2) + ": " + matcher.group(3));
            if (mcmKv == null) {
                continue;
            }

            String inlineComment = null;
            Matcher inlineCommentMatcher = MERGED_MODEL_ATTRIBUTE_PATTERN_WITH_INLINE_COMMENT.matcher(line);
            if (inlineCommentMatcher.matches()) {
                inlineComment = inlineCommentMatcher.group(4);
            }
            Map<String, Pair<Object, String>> modelAttributes = result.computeIfAbsent(modelId, k -> new LinkedHashMap<>());
            modelAttributes.put((String) mcmKv[0], new ImmutablePair<>(mcmKv[1], inlineComment));
        }

        return result;
    }
}