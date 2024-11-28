package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationEndpointDSL;

public interface RelationEndpointDSLMapper {

    RelationEndpointDSL toDSL(Node relationEndpoint);

}
