package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import org.mapstruct.*;

import java.util.LinkedHashMap;

@Mapper(uses = {UmletPositionUxfMapper.class})
public interface ElementUxfMapper {

    // no explicit mapping for mcmAttributes, the custom function below will handle them
    @Mapping(source = "attributes.description", target = "description")
    @Mapping(source = "attributes.umletAttributes", target = "umletAttributes")
    @Mapping(source = "umletPosition", target = "umletPosition")
    @Mapping(source = "attributes.mcmAttributesInlineComments", target = "mcmAttributesInlineComments")
    Node toNode(ElementUxf element, @Context int zoomLevel);

    // explicit mapping for mcmAttributes needed because the custom mapping function only handles the extracted fields
    @Mapping(source = "mcmAttributes", target = "attributes.mcmAttributes")
    @Mapping(source = "umletAttributes", target = "attributes.umletAttributes")
    @Mapping(source = "umletPosition", target = "umletPosition")
    @Mapping(source = "mcmAttributesInlineComments", target = "attributes.mcmAttributesInlineComments")
    ElementUxf fromNode(Node node, @Context int zoomLevel);

    @AfterMapping
    default Node populateMcmFields(ElementUxf elementUxf, @MappingTarget Node n) {
        if (elementUxf.getAttributes() == null) {
            return n;
        }

        n.updateDescriptionAndTitle(elementUxf.getAttributes().getDescription());

        if (elementUxf.getAttributes().getMcmAttributes() == null) {
            return n;
        }
        return McmAttributesMapper.populateFields(elementUxf.getAttributes().getMcmAttributes(), n);
    }

    @AfterMapping
    default ElementUxf combineMcmAttributes(Node node, @MappingTarget ElementUxf elementUxf) {
        if (node.getDescription() != null) {
            elementUxf.getAttributes().setDescription(node.getTitle() + node.getDescription());
        } else {
            elementUxf.getAttributes().setDescription(node.getTitle());
        }

        LinkedHashMap<String, Object> mcmAttrs = McmAttributesMapper.mergeAttributes(node);
        if (elementUxf.getAttributes().getMcmAttributes() == null) {
            elementUxf.getAttributes().setMcmAttributes(new LinkedHashMap<>());
        }
        elementUxf.getAttributes().getMcmAttributes().putAll(mcmAttrs);
        return elementUxf;
    }
}
