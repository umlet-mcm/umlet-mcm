package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.LinkedHashMap;

@Mapper(uses = {UmletPositionUxfMapper.class})
public interface ElementUxfMapper {

    // no explicit mapping for mcmAttributes, they will be populated in the custom function below
    @Mapping(source = "attributes.originalText", target = "originalText")
    @Mapping(source = "attributes.description", target = "description")
    @Mapping(source = "attributes.umletAttributes", target = "umletAttributes")
    Node toNode(ElementUxf element);

    @Mapping(source = "mcmAttributes", target = "attributes.mcmAttributes")
    @Mapping(source = "umletAttributes", target = "attributes.umletAttributes")
    // explicit mapping for mcmAttributes needed because the custom mapping function only handles the extracted fields
    @Mapping(source = "originalText", target = "attributes.originalText")
    ElementUxf fromNode(Node node);

    @AfterMapping
    default Node populateMcmFields(ElementUxf elementUxf, @MappingTarget Node n) {
        n.updateDescriptionAndTitle(elementUxf.getAttributes().getDescription());

        if (elementUxf.getAttributes() == null || elementUxf.getAttributes().getMcmAttributes() == null) {
            return n;
        }
        return McmAttributesMapper.populateFields(elementUxf.getAttributes().getMcmAttributes(), n);
    }

    @AfterMapping
    default ElementUxf combineMcmAttributes(Node node, @MappingTarget ElementUxf elementUxf) {
        elementUxf.getAttributes().setDescription(node.getTitle() + node.getDescription());

        LinkedHashMap<String, Object> mcmAttrs = McmAttributesMapper.mergeAttributes(node);
        if (elementUxf.getAttributes().getMcmAttributes() == null) {
            elementUxf.getAttributes().setMcmAttributes(new LinkedHashMap<>());
        }
        elementUxf.getAttributes().getMcmAttributes().putAll(mcmAttrs);
        return elementUxf;
    }
}
