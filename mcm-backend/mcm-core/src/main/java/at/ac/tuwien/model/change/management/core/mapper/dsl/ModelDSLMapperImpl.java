package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ModelDSLMapperImpl implements ModelDSLMapper {

    private final PropertiesDSLMapper propertiesDSLMapper;

    @Override
    public ModelDSL toDSL(Model model) {
        ModelDSL modelDSL = new ModelDSL();
        modelDSL.setId(model.getId());
        modelDSL.setText(model.getDescription());
        modelDSL.setMcmType(model.getMcmType());
        modelDSL.setProperties(propertiesDSLMapper.toDSL(model.getMcmAttributes()));
        return modelDSL;
    }

    @Override
    public Model fromDSL(ModelDSL modelDSL) {
        Model model = new Model();
        model.setId(modelDSL.getId());
        model.setDescription(modelDSL.getText());
        model.setMcmType(modelDSL.getMcmType());
        model.setMcmAttributes(propertiesDSLMapper.fromDSL(modelDSL.getProperties()));
        return model;
    }
}
