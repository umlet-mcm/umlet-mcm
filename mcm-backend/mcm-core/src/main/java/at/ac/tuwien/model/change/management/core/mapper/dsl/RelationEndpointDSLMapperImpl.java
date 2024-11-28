package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationEndpointDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RelationEndpointDSLMapperImpl implements RelationEndpointDSLMapper {

    @Override
    public RelationEndpointDSL toDSL(Node relationEndpoint) {
        if (relationEndpoint == null) return null;

        RelationEndpointDSL relationEndpointDSL = new RelationEndpointDSL();
        relationEndpointDSL.setId(relationEndpoint.getId());
        relationEndpointDSL.setText(relationEndpoint.getDescription());

        return relationEndpointDSL;
    }
}
