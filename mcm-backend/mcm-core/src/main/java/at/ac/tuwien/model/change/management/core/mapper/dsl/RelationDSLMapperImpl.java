package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.RelativePosition;
import at.ac.tuwien.model.change.management.core.model.dsl.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@AllArgsConstructor
public class RelationDSLMapperImpl implements RelationDSLMapper {
    private final PropertiesDSLMapper propertiesDSLMapper;
    private final RelationEndpointDSLMapper relationEndpointDSLMapper;
    private final CoordinatesDSLMapper coordinatesDSLMapper;
    private final RelativePositionDSLMapper relativePositionDSLMapper;

    @Override
    public RelationDSL toDSL(Relation relation, Node source) throws DSLException {
        if (relation == null) return null;

        RelationEndpointDSL sourceDSL = relationEndpointDSLMapper.toDSL(source);
        RelationEndpointDSL targetDSL = relationEndpointDSLMapper.toDSL(relation.getTarget());

        PositionsDSL positionsDSL = new PositionsDSL();
        positionsDSL.setRelativeStartPoint(relativePositionDSLMapper.toDSL(relation.getRelativeStartPoint()));

        if (relation.getRelativeMidPoints() != null) {
            List<RelativePositionDSL> relativeMidPoints = new ArrayList<>();
            for (RelativePosition relativePosition : relation.getRelativeMidPoints()) {
                relativeMidPoints.add(relativePositionDSLMapper.toDSL(relativePosition));
            }
            positionsDSL.setRelativeMidPoints(relativeMidPoints);
        }
        positionsDSL.setRelativeEndPoint(relativePositionDSLMapper.toDSL(relation.getRelativeEndpoint()));

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setCoordinates(coordinatesDSLMapper.toDSL(relation.getUmletPosition()));
        metadataDSL.setPositions(positionsDSL);

        RelationDSL relationDSL = new RelationDSL();
        relationDSL.setId(relation.getId());
        relationDSL.setText(relation.getDescription());
        relationDSL.setElementType(relation.getType());
        relationDSL.setMcmType(relation.getMcmType());
        relationDSL.setSource(sourceDSL);
        relationDSL.setTarget(targetDSL);
        relationDSL.setProperties(propertiesDSLMapper.toDSL(relation.getMcmAttributes()));
        relationDSL.setMetadata(metadataDSL);

        return relationDSL;
    }

    @Override
    public Relation fromDSL(RelationDSL relationDSL, Node target) throws DSLException {
        if (relationDSL == null) {
            return null;
        }

        if (relationDSL.getMetadata() == null) {
            throw new DSLException("Metadata of the relation " + relationDSL.getId() + " cannot be null");
        }

        if (relationDSL.getMetadata().getPositions() == null) {
            throw new DSLException("Positions of the relation " + relationDSL.getId() + " cannot be null");
        }

        Relation relation = new Relation();

        relation.setId(relationDSL.getId());
        relation.setDescription(relationDSL.getText());
        relation.setTarget(target);
        relation.setType(relationDSL.getElementType());
        relation.setMcmType(relationDSL.getMcmType());
        relation.setUmletPosition(coordinatesDSLMapper.fromDSL(relationDSL.getMetadata().getCoordinates()));

        if (relationDSL.getProperties() != null) {
            relation.setMcmAttributes(propertiesDSLMapper.fromDSL(relationDSL.getProperties()));
        }

        PositionsDSL positionsDSL = relationDSL.getMetadata().getPositions();

        relation.setRelativeStartPoint(relativePositionDSLMapper.fromDSL(positionsDSL.getRelativeStartPoint()));
        if (positionsDSL.getRelativeMidPoints() != null) {
            List<RelativePosition> relativeMidPoints = new ArrayList<>();
            for (RelativePositionDSL relativePositionDSL : positionsDSL.getRelativeMidPoints()) {
                relativeMidPoints.add(relativePositionDSLMapper.fromDSL(relativePositionDSL));
            }
            relation.setRelativeMidPoints(relativeMidPoints);
        }
        relation.setRelativeEndPoint(relativePositionDSLMapper.fromDSL(relationDSL.getMetadata().getPositions().getRelativeEndPoint()));

        return relation;
    }
}
