package at.ac.tuwien.model.change.management.core.transformer;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;

import java.util.Set;

public interface DSLTransformer {

    Set<Node> parseToNodes(Set<String> nodes, Set<String> relations) throws DSLException;

    String parseToNodeDSL(Node node) throws DSLException;

    String parseToRelationDSL(Relation relation, Node source) throws DSLException;

    Model parseToModel(String metadata) throws DSLException;

    String parseToModelDSL(Model model) throws DSLException;
}
