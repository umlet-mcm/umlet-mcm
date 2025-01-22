package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ConfigurationUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import at.ac.tuwien.model.change.management.core.model.utils.PositionUtils;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ConfigurationUxfMapper {

    public static ConfigurationUxf toConfigurationUxf(Configuration configuration) {
        ConfigurationUxf configurationUxf = new ConfigurationUxf();

        ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);

        configurationUxf.setElements(new LinkedHashSet<>());

        // get new zoom level, take the smallest value from the models
        int newZoomLevel = configuration.getModels().stream().
                mapToInt(Model::getZoomLevel).
                min().
                orElse(PositionUtils.DEFAULT_ZOOM_LEVEL);

        configurationUxf.setZoomLevel(newZoomLevel);

        // map the models
        ArrayList<ModelUxf> mappedModels = new ArrayList<>();
        for (Model m : configuration.getModels()) {
            m.setZoomLevel(newZoomLevel); // update the zoom level so during the mapping the coordinates are updated
            ModelUxf modelUxf = modelUxfMapper.fromModel(m);
            mappedModels.add(modelUxf);
        }

        PositionUtils.alignModels(mappedModels);

        for (ModelUxf m : mappedModels) {
            configurationUxf.getElements().addAll(m.getElements());
        }

        // set description
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration ").append(configuration.getName()).append(" exported from MCM\n");
        sb.append("// ").append(AttributeKeys.CONFIGURATION_ID)
                .append(": ")
                .append(ParserUtils.formatMcmValueForExport(configuration.getName()))
                .append("\n");
        sb.append("--Attributes from models--\n");
        sb.append(combineModelDescriptions(configuration.getModels()));
        configurationUxf.setAttributes(new BaseAttributesUxf());
        configurationUxf.getAttributes().setDescription(sb.toString());


        return configurationUxf;
    }

    public static String combineModelDescriptions(Set<Model> models) {
        StringBuilder sb = new StringBuilder();
        for (Model m : models) {
            // add model title
            sb.append(stringifyModelValue(m.getId(), AttributeKeys.TITLE, m.getTitle())).append("\n");

            // add model ID and tags
            Map<String, Object> mergedAttribs = McmAttributesMapper.mergeAttributes(m);
            if (m.getMcmAttributes() != null) {
                mergedAttribs.putAll(m.getMcmAttributes());
            }

            // use format: __modelId_key: value
            for (var kv : mergedAttribs.entrySet()) {
                sb.append(stringifyModelValue(m.getId(), kv.getKey(), kv.getValue()));

                if (m.getMcmAttributesInlineComments() != null && m.getMcmAttributesInlineComments().get(kv.getKey()) != null) {
                    sb.append(" ").append(m.getMcmAttributesInlineComments().get(kv.getKey()));
                }

                sb.append("\n");

            }
        }

        return sb.toString();
    }

    private static String stringifyModelValue(String modelID, String key, Object value) {
        if (modelID == null || key == null) return ""; // append string 'null' if value is null
        return "// __" + modelID + "_" + key + ": " + ParserUtils.formatMcmValueForExport(value);
    }
}