package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.server.dto.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class MapperTest {

    protected NodeDTO getNodeDTO(Set<RelationDTO> relationDTOs, String id, String modelID) {
        return new NodeDTO(
                "node",
                List.of(1, 2),
                new UMLetPositionDTO(1, 2, 3, 4),
                relationDTOs,
                id,
                List.of("tag1", "tag2"),
                "original text",
                "title",
                "description",
                new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")),
                "mcmModel",
                modelID,
                new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")),
                "pprType"
        );
    }

    protected RelationDTO getRelationDTO(NodeDTO targetDTO, String id, String modelId) {
        return new RelationDTO(
                "RelationType1",
                targetDTO,
                new UMLetPositionDTO(50, 60, 70, 80),
                new RelativePositionDTO(5, 5, 2, 2),
                List.of(
                        new RelativePositionDTO(10, 15, 5, 5),
                        new RelativePositionDTO(20, 25, 10, 10)
                ),
                new RelativePositionDTO(30, 35, 15, 15),
                new PointDTO(0, 0),
                new PointDTO(100, 100),
                id,
                List.of("relationTag1"),
                "Original relation text",
                "Relation Title",
                "Relation Description",
                new LinkedHashMap<>(),
                "MCM_Model_Relation_1",
                modelId,
                new LinkedHashMap<>(),
                "PPRTypeRelation1"
        );
    }

    protected ModelDTO getModelDTO(Set<NodeDTO> nodeDTOs, String id) {
        return new ModelDTO(
                nodeDTOs,
                id,
                List.of("tag1", "tag2"),
                "original text",
                "title",
                "description",
                new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")),
                20
        );
    }

    protected Node getNode(Set<Relation> relations, String id, String modelId) {
        Node node = new Node();
        node.setId(id);
        node.setTags(List.of("tag1", "tag2"));
        node.setOriginalText("Original text of the node");
        node.setTitle("Node Title");
        node.setDescription("Node Description");
        node.setMcmAttributes(new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")));
        node.setPprType("pprType");
        node.setElementType("elementType");
        node.setRelations(relations);
        node.setUmletPosition(new UMLetPosition(1, 2, 3, 4));
        node.setMcmModel("mcmModel");
        node.setMcmModelId(modelId);
        node.setGeneratedAttributes(List.of(1, 2));
        node.setUmletAttributes(new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")));

        return node;
    }

    protected Relation getRelation(Node target, String id, String modelId) {
        Relation relation = new Relation();
        relation.setId(id);
        relation.setTarget(target);
        relation.setType("relation-type");
        relation.setTags(List.of("tag1", "tag2"));
        relation.setTitle("Relation Title");
        relation.setMcmModel("model-123");
        relation.setMcmModelId(modelId);
        relation.setUmletAttributes(new LinkedHashMap<>(Map.of("key1", "value1", "key2", "value2")));
        relation.setOriginalText("Original text of the relation");
        relation.setPprType("ppr-type");
        relation.setUmletPosition(new UMLetPosition(1, 2, 3, 4));
        relation.setRelativeEndPoint(new RelativePosition(1, 2, 3, 4));
        relation.setRelativeStartPoint(new RelativePosition(1, 2, 3, 4));
        relation.setRelativeMidPoints(List.of(
                new RelativePosition(1, 2, 3, 4),
                new RelativePosition(1, 2, 3, 4)
        ));
        relation.setStartPoint(new Point(1, 2));
        relation.setEndPoint(new Point(1, 2));

        return relation;
    }
}
