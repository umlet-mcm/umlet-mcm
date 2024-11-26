package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UmletPositionUxfMapper.class})
public interface ElementUxfMapper {
    @Mapping(source = "attributes.description", target = "description")
    @Mapping(source = "attributes.mcmAttributes", target = "mcmAttributes")
    @Mapping(source = "attributes.umletAttributes", target = "umletAttributes")
    @Mapping(source = "attributes.originalText", target = "originalText")
    Node toNode(ElementUxf element);

    @Mapping(source = "description", target = "attributes.description")
    @Mapping(source = "mcmAttributes", target = "attributes.mcmAttributes")
    @Mapping(source = "umletAttributes", target = "attributes.umletAttributes")
    @Mapping(source = "originalText", target = "attributes.originalText")
    ElementUxf fromNode(Node node);
}
