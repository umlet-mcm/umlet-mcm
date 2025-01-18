package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.intermediary.BaseAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ConfigurationUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.utils.PositionUtils;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConfigurationUxfMapper {

    public static ConfigurationUxf toConfigurationUxf(Configuration configuration) {
        ConfigurationUxf configurationUxf = new ConfigurationUxf();

        ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);

        configurationUxf.setElements(new LinkedHashSet<>());

        // map the models
        ArrayList<ModelUxf> mappedModels = new ArrayList<>();
        for (Model m : configuration.getModels()) {
            ModelUxf modelUxf = modelUxfMapper.fromModel(m);
            mappedModels.add(modelUxf);
        }

        PositionUtils.alignModels(mappedModels);

        for(ModelUxf m : mappedModels) {
            configurationUxf.getElements().addAll(m.getElements());
        }

        // set description
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration ").append(configuration.getName()).append(" exported from MCM\n");
        sb.append("//////////\n");
        sb.append(combineModelDescriptions(configuration.getModels()));
        configurationUxf.setAttributes(new BaseAttributesUxf());
        configurationUxf.getAttributes().setDescription(sb.toString());

        // set zoom level, take the smallest value from the models
        configurationUxf.setZoomLevel(configuration.getModels().stream().
                mapToInt(Model::getZoomLevel).
                min().
                orElse(10));


        return configurationUxf;
    }

    public static String combineModelDescriptions(Set<Model> models) {
        StringBuilder sb = new StringBuilder();
        for (Model m : models) {
            sb.append("Model ").append(m.getId()).append("\n");
            if (m.getDescription() != null) {
                sb.append(m.getDescription());
                if(!m.getDescription().endsWith("\n")){
                    sb.append("\n");
                }
            }

            sb.append("//////////\n");
        }

        return sb.toString();
    }
}