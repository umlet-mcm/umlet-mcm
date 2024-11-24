package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;

public interface ModelDSLMapper {

    ModelDSL toDSL(Model model);

    Model fromDSL(ModelDSL modelDSL);
}
