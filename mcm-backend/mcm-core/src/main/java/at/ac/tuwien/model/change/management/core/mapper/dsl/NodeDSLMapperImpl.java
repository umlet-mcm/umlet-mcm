package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.NodeDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@AllArgsConstructor
public class NodeDSLMapperImpl implements NodeDSLMapper {
    private final CoordinatesDSLMapper coordinatesDSLMapper;
    private final KeyValuesDSLMapper keyValuesDSLMapper;

    @Override
    public NodeDSL toDSL(Node node) {
        if (node == null) {
            return null;
        }

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setPanelAttributes(keyValuesDSLMapper.toStringDSL(node.getUmletAttributes()));
        metadataDSL.setAdditionalAttributes(node.getGeneratedAttributes());
        metadataDSL.setCoordinates(coordinatesDSLMapper.toDSL(node.getUmletPosition()));

        NodeDSL nodeDSL = new NodeDSL();
        nodeDSL.setId(node.getId());
        nodeDSL.setMcmModel(node.getMcmModel());
        nodeDSL.setMcmModelId(node.getMcmModelId());
        nodeDSL.setTitle(node.getTitle());
        nodeDSL.setDescription(node.getDescription());
        nodeDSL.setElementType(node.getElementType());
        nodeDSL.setPprType(node.getPprType());
        nodeDSL.setProperties(keyValuesDSLMapper.toObjectDSL(node.getMcmAttributes()));
        nodeDSL.setPropertiesInlineComments(keyValuesDSLMapper.toStringDSL(node.getMcmAttributesInlineComments()));
        if (node.getTags() != null && !node.getTags().isEmpty()) {
            nodeDSL.setTags(node.getTags());
        }
        nodeDSL.setMetadata(metadataDSL);

        return nodeDSL;
    }

    @Override
    public Node fromDSL(NodeDSL nodeDSL) {
        if (nodeDSL == null) {
            return null;
        }

        Node node = new Node();
        node.setId(nodeDSL.getId());
        node.setMcmModel(nodeDSL.getMcmModel());
        node.setMcmModelId(nodeDSL.getMcmModelId());
        node.setTitle(nodeDSL.getTitle());
        node.setDescription(nodeDSL.getDescription());
        node.setElementType(nodeDSL.getElementType());
        node.setPprType(nodeDSL.getPprType());

        if (nodeDSL.getMetadata() != null) {
            node.setUmletPosition(coordinatesDSLMapper.fromDSL(nodeDSL.getMetadata().getCoordinates()));
            node.setUmletAttributes(keyValuesDSLMapper.fromStringDSL(nodeDSL.getMetadata().getPanelAttributes()));
            node.setGeneratedAttributes(nodeDSL.getMetadata().getAdditionalAttributes());
        }

        node.setMcmAttributes(keyValuesDSLMapper.fromObjectDSL(nodeDSL.getProperties()));
        node.setMcmAttributesInlineComments(keyValuesDSLMapper.fromStringDSL(nodeDSL.getPropertiesInlineComments()));
        node.setTags(nodeDSL.getTags() == null ? Collections.emptyList() : nodeDSL.getTags());


        return node;
    }
}
