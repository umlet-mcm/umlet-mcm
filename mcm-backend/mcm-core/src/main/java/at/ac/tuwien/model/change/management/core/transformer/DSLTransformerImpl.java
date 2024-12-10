package at.ac.tuwien.model.change.management.core.transformer;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.mapper.dsl.ModelDSLMapper;
import at.ac.tuwien.model.change.management.core.mapper.dsl.NodeDSLMapper;
import at.ac.tuwien.model.change.management.core.mapper.dsl.RelationDSLMapper;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.NodeDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationEndpointDSL;
import at.ac.tuwien.model.change.management.core.utils.ParsingUtils;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DSLTransformerImpl implements DSLTransformer {

    private final NodeDSLMapper nodeDSLMapper;
    private final RelationDSLMapper relationDSLMapper;
    private final ModelDSLMapper modelDSLMapper;

    @Override
    public Set<Node> parseToNodes(Set<String> nodesDSL, Set<String> relationsDSL) throws DSLException {
        Map<String, Node> nodes = new HashMap<>();

        // Parse nodes
        for (String textualNodeDSL : nodesDSL) {
            try {
                NodeDSL nodeDSL = (NodeDSL) ParsingUtils.unmarshalDSL(textualNodeDSL);
                Node node = nodeDSLMapper.fromDSL(nodeDSL);
                nodes.put(node.getId(), node);
            } catch (JAXBException e) {
                throw new DSLException("Failed to parse node DSL: " + textualNodeDSL, e);
            }
        }

        // Parse relation & attach to source node
        for (String textualRelationDSL : relationsDSL) {
            try {
                RelationDSL relationDSL = (RelationDSL) ParsingUtils.unmarshalDSL(textualRelationDSL);

                if (relationDSL.getSource() == null || nodes.get(relationDSL.getSource().getId()) == null) {
                    throw new DSLException("Source node is null for relation : " + relationDSL.getId());
                }

                Node sourceNode = nodes.get(relationDSL.getSource().getId());

                Node targetNode = Optional.ofNullable(relationDSL.getTarget())
                        .map(RelationEndpointDSL::getId)
                        .map(nodes::get)
                        .orElse(null);

                Relation relation = relationDSLMapper.fromDSL(relationDSL, targetNode);
                sourceNode.getRelations().add(relation);
            } catch (JAXBException e) {
                throw new DSLException("Failed to parse relation DSL: " + textualRelationDSL, e);
            }
        }

        return new HashSet<>(nodes.values());
    }

    @Override
    public String parseToNodeDSL(Node node) throws DSLException {
        try {
            NodeDSL nodeDSL = nodeDSLMapper.toDSL(node);
            return ParsingUtils.marshalDSL(nodeDSL);
        } catch (JAXBException e) {
            throw new DSLException("Failed to parse node to DSL: " + node, e);
        }
    }

    @Override
    public String parseToRelationDSL(Relation relation, Node source) throws DSLException {
        try {
            RelationDSL relationDSL = relationDSLMapper.toDSL(relation, source);
            return ParsingUtils.marshalDSL(relationDSL);
        } catch (JAXBException e) {
            throw new DSLException("Failed to parse relation to DSL: " + relation, e);
        }
    }

    @Override
    public Model parseToModel(String modelDSLText) throws DSLException {
        try {
            ModelDSL modelDSL = (ModelDSL) ParsingUtils.unmarshalDSL(modelDSLText);
            return modelDSLMapper.fromDSL(modelDSL);
        } catch (JAXBException e) {
            throw new DSLException("Failed to parse model DSL: " + modelDSLText, e);
        }
    }

    @Override
    public String parseToModelDSL(Model model) throws DSLException {
        try {
            ModelDSL modelDSL = modelDSLMapper.toDSL(model);
            return ParsingUtils.marshalDSL(modelDSL);
        } catch (JAXBException e) {
            throw new DSLException("Failed to parse model to DSL: " + model, e);
        }
    }
}
