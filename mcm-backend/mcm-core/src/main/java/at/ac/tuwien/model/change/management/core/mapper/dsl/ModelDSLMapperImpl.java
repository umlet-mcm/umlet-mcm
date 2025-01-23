package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@AllArgsConstructor
public class ModelDSLMapperImpl implements ModelDSLMapper {

    private final KeyValuesDSLMapper keyValuesDSLMapper;

    @Override
    public ModelDSL toDSL(Model model) {
        ModelDSL modelDSL = new ModelDSL();
        modelDSL.setId(model.getId());
        modelDSL.setTitle(model.getTitle());
        modelDSL.setDescription(model.getDescription());

        if (model.getTags() != null && !model.getTags().isEmpty()) {
            modelDSL.setTags(model.getTags());
        }
        modelDSL.setProperties(keyValuesDSLMapper.toObjectDSL(model.getMcmAttributes()));
        modelDSL.setPropertiesInlineComments(keyValuesDSLMapper.toStringDSL(model.getMcmAttributesInlineComments()));
        modelDSL.setZoomLevel(model.getZoomLevel());

        MetadataDSL metadataDSL = new MetadataDSL();

        modelDSL.setMetadata(metadataDSL);

        return modelDSL;
    }

    @Override
    public Model fromDSL(ModelDSL modelDSL) {
        Model model = new Model();
        model.setId(modelDSL.getId());
        model.setDescription(modelDSL.getDescription());
        model.setTitle(modelDSL.getTitle());
        model.setTags(modelDSL.getTags() == null ? Collections.emptyList() : modelDSL.getTags());
        model.setMcmAttributes(keyValuesDSLMapper.fromObjectDSL(modelDSL.getProperties()));
        model.setMcmAttributesInlineComments(keyValuesDSLMapper.fromStringDSL(modelDSL.getPropertiesInlineComments()));
        model.setZoomLevel(modelDSL.getZoomLevel());
        return model;
    }
}
