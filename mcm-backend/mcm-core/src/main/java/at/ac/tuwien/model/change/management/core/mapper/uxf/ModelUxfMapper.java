package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ParserUtils;
import at.ac.tuwien.model.change.management.core.model.utils.RelationUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

@Mapper(uses = {ElementUxfMapper.class})
public interface ModelUxfMapper {
    @Mapping(source = "attributes.description", target = "description")
    @Mapping(source = "elements", target = "nodes")
    Model _toModel(ModelUxf modelUxf, @Context int zoomLevel);

    // Proxy, needed because of the context
    default Model toModel(ModelUxf modelUxf) {
        return _toModel(modelUxf, modelUxf.zoomLevel);
    }

    @Mapping(source = "description", target = "attributes.description")
    @Mapping(source = "mcmAttributes", target = "attributes.mcmAttributes")
    @Mapping(source = "nodes", target = "elements")
    ModelUxf _fromModel(Model model, @Context int zoomLevel);

    // Proxy, needed because of the context
    default ModelUxf fromModel(Model model) {
        return _fromModel(model, model.getZoomLevel());
    }

    /**
     * The relations stored in the nodes must be converted back to elements. Bidirectional
     * relations that were previously split should now be merged back together.
     */
    @AfterMapping
    default ModelUxf processRelations(Model m, @MappingTarget ModelUxf modelUxf) {
        if (m.getNodes() == null) {
            return modelUxf;
        }

        // collect relations
        LinkedHashSet<Relation> relations = new LinkedHashSet<>();
        for (Node n : m.getNodes()) {
            if (!n.getRelations().isEmpty()) {
                relations.addAll(n.getRelations());
            }
        }

        ArrayList<ElementUxf> convertedRelations = new ArrayList<>();
        // convert relations to elements
        UmletPositionUxfMapper umletPositionUxfMapper = Mappers.getMapper(UmletPositionUxfMapper.class);
        for (Relation r : relations) {
            ElementUxf relationElement = new ElementUxf();
            relationElement.setElementType("Relation");
            ElementAttributesUxf elAttrs = new ElementAttributesUxf();
            elAttrs.setUmletAttributes(r.getUmletAttributes());
            elAttrs.setOriginalText(r.getOriginalText());
            elAttrs.setDescription(r.getTitle() + r.getDescription());
            elAttrs.setMcmAttributes(r.getMcmAttributes());
            relationElement.setAttributes(elAttrs);

            UmletPositionUxf positionUxf = umletPositionUxfMapper.fromUmletPosition(r.getUmletPosition(), m.getZoomLevel());
            relationElement.setUmletPosition(positionUxf);

            // denormalize coordinates
            var genCoords = r.getGeneratedAttributes();
            genCoords.replaceAll(val -> ParserUtils.denormalizeCoordinate(val, m.getZoomLevel()));
            relationElement.setGeneratedAttributes(genCoords);

            LinkedHashMap<String, Object> mcmAttrs = McmAttributesMapper.mergeAttributes(r);
            if (relationElement.getAttributes().getMcmAttributes() == null) {
                relationElement.getAttributes().setMcmAttributes(new LinkedHashMap<>());
            }
            relationElement.getAttributes().getMcmAttributes().putAll(mcmAttrs);

            convertedRelations.add(relationElement);
        }

        // merge relations if they are the same
        ArrayList<ElementUxf> mergedList = new ArrayList<>();
        for (ElementUxf r : convertedRelations) {
            ElementUxf mergeWith = mergedList.stream()
                    .filter(o -> o.getUmletPosition().equals(r.getUmletPosition()) &&
                            o.getAttributes().getMcmAttributes().get(AttributeKeys.ID).equals(
                                    r.getAttributes().getMcmAttributes().get(AttributeKeys.ID)
                            ))
                    .findFirst()
                    .orElse(null);
            if (mergeWith == null) {
                mergedList.add(r);
            } else {
                mergedList.remove(mergeWith);
                ElementUxf res = RelationUtils.mergeRelationElements(r, mergeWith);
                mergedList.add(res);
            }
        }

        modelUxf.getElements().addAll(mergedList);

        LinkedHashMap<String, Object> mergedAttrs = McmAttributesMapper.mergeAttributes(m);
        if (modelUxf.getAttributes().getMcmAttributes() == null) {
            modelUxf.getAttributes().setMcmAttributes(new LinkedHashMap<>());
        }
        modelUxf.getAttributes().getMcmAttributes().putAll(mergedAttrs);

        return modelUxf;
    }

    @AfterMapping
    default Model populateMcmFields(ModelUxf modelUxf, @MappingTarget Model model) {
        if (modelUxf.getAttributes() == null || modelUxf.getAttributes().getMcmAttributes() == null) {
            return model;
        }
        return McmAttributesMapper.populateFields(modelUxf.getAttributes().getMcmAttributes(), model);
    }
}
