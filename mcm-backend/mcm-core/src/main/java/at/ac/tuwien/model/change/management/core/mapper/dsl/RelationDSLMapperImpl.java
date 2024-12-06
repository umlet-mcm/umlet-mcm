package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.PositionsDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationEndpointDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
public class RelationDSLMapperImpl implements RelationDSLMapper {
    private final PropertiesDSLMapper propertiesDSLMapper;
    private final PanelAttributesDSLMapper panelAttributesDSLMapper;
    private final RelationEndpointDSLMapper relationEndpointDSLMapper;
    private final CoordinatesDSLMapper coordinatesDSLMapper;
    private final RelativePositionDSLMapper relativePositionDSLMapper;

    @Override
    public RelationDSL toDSL(Relation relation, Node source) {
        if (relation == null) return null;

        RelationEndpointDSL sourceDSL = relationEndpointDSLMapper.toDSL(source);
        RelationEndpointDSL targetDSL = relationEndpointDSLMapper.toDSL(relation.getTarget());

        RelationDSL relationDSL = new RelationDSL();
        relationDSL.setId(relation.getId());
        relationDSL.setMcmModel(relation.getMcmModel());
        relationDSL.setMcmModelId(relation.getMcmModelId());
        relationDSL.setTitle(relation.getTitle());
        relationDSL.setDescription(relation.getDescription());
        relationDSL.setElementType(relation.getType());
        relationDSL.setPprType(relation.getPprType());
        relationDSL.setSource(sourceDSL);
        relationDSL.setTarget(targetDSL);
        relationDSL.setProperties(propertiesDSLMapper.toDSL(relation.getMcmAttributes()));
        relationDSL.setTags(relation.getTags());

        PositionsDSL positionsDSL = new PositionsDSL();
        positionsDSL.setRelativeStartPoint(relativePositionDSLMapper.toDSL(relation.getRelativeStartPoint()));
        positionsDSL.setRelativeMidPoints(
                Optional.ofNullable(relation.getRelativeMidPoints())
                        .map(midPoints -> midPoints.stream()
                                .map(relativePositionDSLMapper::toDSL)
                                .collect(Collectors.toList()))
                        .orElse(null));
        positionsDSL.setRelativeEndPoint(relativePositionDSLMapper.toDSL(relation.getRelativeEndPoint()));

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setCoordinates(coordinatesDSLMapper.toDSL(relation.getUmletPosition()));
        metadataDSL.setPanelAttributes(panelAttributesDSLMapper.toDSL(relation.getUmletAttributes()));
        metadataDSL.setPositions(positionsDSL);
        metadataDSL.setOriginalText(relation.getOriginalText());
        relationDSL.setMetadata(metadataDSL);

        return relationDSL;
    }

    @Override
    public Relation fromDSL(RelationDSL relationDSL, Node target) {
        if (relationDSL == null) {
            return null;
        }

        Relation relation = new Relation();

        relation.setId(relationDSL.getId());
        relation.setMcmModel(relationDSL.getMcmModel());
        relation.setMcmModelId(relationDSL.getMcmModelId());
        relation.setTitle(relationDSL.getTitle());
        relation.setDescription(relationDSL.getDescription());
        relation.setTarget(target);
        relation.setType(relationDSL.getElementType());
        relation.setPprType(relationDSL.getPprType());
        relation.setMcmAttributes(propertiesDSLMapper.fromDSL(relationDSL.getProperties()));
        relation.setTags(relationDSL.getTags());

        Optional.ofNullable(relationDSL.getMetadata()).ifPresent(metadata -> {
            relation.setUmletPosition(coordinatesDSLMapper.fromDSL(metadata.getCoordinates()));
            relation.setUmletAttributes(panelAttributesDSLMapper.fromDSL(metadata.getPanelAttributes()));
            relation.setOriginalText(metadata.getOriginalText());

            Optional.ofNullable(metadata.getPositions()).ifPresent(positionsDSL -> {
                relation.setRelativeStartPoint(
                        relativePositionDSLMapper.fromDSL(positionsDSL.getRelativeStartPoint())
                );

                List<RelativePosition> relativeMidPoints = Optional.ofNullable(positionsDSL.getRelativeMidPoints())
                        .map(midPoints -> midPoints.stream()
                                .map(relativePositionDSLMapper::fromDSL)
                                .collect(Collectors.toList()))
                        .orElseGet(Collections::emptyList);

                relation.setRelativeMidPoints(relativeMidPoints);
                relation.setRelativeEndPoint(
                        relativePositionDSLMapper.fromDSL(positionsDSL.getRelativeEndPoint())
                );
            });
        });

        return relation;
    }
}
