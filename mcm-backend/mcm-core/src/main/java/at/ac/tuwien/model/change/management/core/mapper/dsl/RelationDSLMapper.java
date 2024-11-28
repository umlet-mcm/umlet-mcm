package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationDSL;

public interface RelationDSLMapper {

    RelationDSL toDSL(Relation relation, Node source) throws DSLException;

    Relation fromDSL(RelationDSL relationDSL, Node target) throws DSLException;
}
