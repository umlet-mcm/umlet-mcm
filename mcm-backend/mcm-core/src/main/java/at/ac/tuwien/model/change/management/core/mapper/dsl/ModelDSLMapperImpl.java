package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ModelDSLMapperImpl implements ModelDSLMapper {

    private final PropertiesDSLMapper propertiesDSLMapper;

    @Override
    public ModelDSL toDSL(Model model) {
        ModelDSL modelDSL = new ModelDSL();
        modelDSL.setId(model.getId());
        modelDSL.setTitle(model.getTitle());
        modelDSL.setDescription(model.getDescription());
        modelDSL.setTags(model.getTags());
        modelDSL.setProperties(propertiesDSLMapper.toDSL(model.getMcmAttributes()));
        modelDSL.setZoomLevel(model.getZoomLevel());

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setOriginalText(model.getOriginalText());

        modelDSL.setMetadata(metadataDSL);

        return modelDSL;
    }

    @Override
    public Model fromDSL(ModelDSL modelDSL) {
        Model model = new Model();
        model.setId(modelDSL.getId());
        model.setDescription(modelDSL.getDescription());
        model.setTitle(modelDSL.getTitle());
        model.setTags(modelDSL.getTags());
        model.setMcmAttributes(propertiesDSLMapper.fromDSL(modelDSL.getProperties()));
        model.setOriginalText(Optional.ofNullable(modelDSL.getMetadata())
                .map(MetadataDSL::getOriginalText)
                .orElse(null));
        model.setZoomLevel(modelDSL.getZoomLevel());
        return model;
    }
}
