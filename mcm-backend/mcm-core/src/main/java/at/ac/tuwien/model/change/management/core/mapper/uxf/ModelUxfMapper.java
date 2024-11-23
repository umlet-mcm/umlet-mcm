package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ElementUxfMapper.class})
public interface ModelUxfMapper {
    @Mapping(source = "attributes.description", target = "description")
    @Mapping(source = "attributes.mcmAttributes", target = "mcmAttributes")
    @Mapping(source = "elements", target = "nodes")
    Model toModel(ModelUxf modelUxf);

    @Mapping(source = "description", target = "attributes.description")
    @Mapping(source = "mcmAttributes", target = "attributes.mcmAttributes")
    @Mapping(source = "nodes", target = "elements")
    ModelUxf fromModel(Model model);
}
