package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ElementUxfMapper {
    @Mapping(source = "attributes.description", target = "description")
    @Mapping(source = "attributes.mcmAttributes", target = "mcmAttributes")
    @Mapping(source = "attributes.umletAttributes", target = "umletAttributes")
    Node toNode(ElementUxf element);

    @Mapping(source = "description", target = "attributes.description")
    @Mapping(source = "mcmAttributes", target = "attributes.mcmAttributes")
    @Mapping(source = "umletAttributes", target = "attributes.umletAttributes")
    ElementUxf fromNode(Node node);
}
