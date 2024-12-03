package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementAttributesUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ElementUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.UmletPositionUxf;
import at.ac.tuwien.model.change.management.core.model.utils.RelationUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

@Mapper(uses = {ElementUxfMapper.class})
public interface ModelUxfMapper {
    @Mapping(source = "attributes.description", target = "description")
    @Mapping(source = "_zoomLevel", target = "zoomLevel")
    @Mapping(source = "elements", target = "nodes")
    Model toModel(ModelUxf modelUxf);

    @Mapping(source = "description", target = "attributes.description")
    @Mapping(source = "mcmAttributes", target = "attributes.mcmAttributes")
    @Mapping(source = "nodes", target = "elements")
    @Mapping(source = "zoomLevel", target = "_zoomLevel")
    ModelUxf fromModel(Model model);

    /**
     * The relations stored in the nodes must be converted back to elements. Bidirectional
     * relations that were previously split should now be merged back together.
     */
    @AfterMapping
    default ModelUxf processRelations(Model m, @MappingTarget ModelUxf modelUxf) {
        // collect relations
        LinkedHashSet<Relation> relations = new LinkedHashSet<>();
        for (Node n : m.getNodes()) {
            if (!n.getRelations().isEmpty()) {
                relations.addAll(n.getRelations());
            }
        }

        ArrayList<ElementUxf> convertedRelations = new ArrayList<>();
        // convert relations to elements
        for (Relation r : relations) {
            // getting an instance of the mappers here does not seem doable,
            // do the mapping by hand
            ElementUxf relationElement = new ElementUxf();
            relationElement.setElementType("Relation");
            ElementAttributesUxf elAttrs = new ElementAttributesUxf();
            elAttrs.setUmletAttributes(r.getUmletAttributes());
            elAttrs.setOriginalText(r.getOriginalText());
            elAttrs.setDescription(r.getTitle() + r.getDescription());
            elAttrs.setMcmAttributes(r.getMcmAttributes());
            relationElement.setAttributes(elAttrs);

            UmletPositionUxf positionUxf = new UmletPositionUxf();
            positionUxf.setX(r.getUmletPosition().getX());
            positionUxf.setY(r.getUmletPosition().getY());
            positionUxf.setWidth(r.getUmletPosition().getWidth());
            positionUxf.setHeight(r.getUmletPosition().getHeight());
            relationElement.setUmletPosition(positionUxf);
            relationElement.setGeneratedAttributes(r.getGeneratedAttributes());

            LinkedHashMap<String, Object> mcmAttrs = McmAttributesMapper.mergeAttributes(r);
            if (relationElement.getAttributes().getMcmAttributes() == null) {
                relationElement.getAttributes().setMcmAttributes(new LinkedHashMap<>());
            }
            relationElement.getAttributes().getMcmAttributes().putAll(mcmAttrs);

            convertedRelations.add(relationElement);
        }

        // merge relations if their umletPosition is the same
        ArrayList<ElementUxf> mergedList = new ArrayList<>();
        for (ElementUxf r : convertedRelations) {
            ElementUxf mergeWith = mergedList.stream()
                    .filter(o -> o.getUmletPosition().equals(r.getUmletPosition()))
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
