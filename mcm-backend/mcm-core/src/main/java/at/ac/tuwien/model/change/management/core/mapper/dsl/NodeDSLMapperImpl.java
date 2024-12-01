package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.NodeDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NodeDSLMapperImpl implements NodeDSLMapper {
    private final PanelAttributesDSLMapper panelAttributesDSLMapper;
    private final CoordinatesDSLMapper coordinatesDSLMapper;
    private final PropertiesDSLMapper propertiesDSLMapper;

    @Override
    public NodeDSL toDSL(Node node) {
        if (node == null) {
            return null;
        }

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setOriginalText(node.getOriginalText());
        metadataDSL.setPanelAttributes(panelAttributesDSLMapper.toDSL(node.getUmletAttributes()));
        metadataDSL.setAdditionalAttributes(node.getGeneratedAttributes());
        metadataDSL.setCoordinates(coordinatesDSLMapper.toDSL(node.getUmletPosition()));

        NodeDSL nodeDSL = new NodeDSL();
        nodeDSL.setId(node.getId());
        nodeDSL.setMcmModel(node.getMcmModel());
        nodeDSL.setTitle(node.getTitle());
        nodeDSL.setDescription(node.getDescription());
        nodeDSL.setElementType(node.getElementType());
        nodeDSL.setPprType(node.getPprType());
        nodeDSL.setProperties(propertiesDSLMapper.toDSL(node.getMcmAttributes()));
        nodeDSL.setTags(node.getTags());
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
        node.setTitle(nodeDSL.getTitle());
        node.setDescription(nodeDSL.getDescription());
        node.setElementType(nodeDSL.getElementType());
        node.setPprType(nodeDSL.getPprType());

        if (nodeDSL.getMetadata() != null) {
            node.setUmletPosition(coordinatesDSLMapper.fromDSL(nodeDSL.getMetadata().getCoordinates()));
            node.setUmletAttributes(panelAttributesDSLMapper.fromDSL(nodeDSL.getMetadata().getPanelAttributes()));
            node.setGeneratedAttributes(nodeDSL.getMetadata().getAdditionalAttributes());
            node.setOriginalText(nodeDSL.getMetadata().getOriginalText());
        }

        node.setMcmAttributes(propertiesDSLMapper.fromDSL(nodeDSL.getProperties()));
        node.setTags(nodeDSL.getTags());

        return node;
    }
}
