package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.dsl.NodeDSL;

public interface NodeDSLMapper {

    NodeDSL toDSL(Node node);

    Node fromDSL(NodeDSL nodeDSL);
}
