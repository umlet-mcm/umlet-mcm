package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.dsl.MetadataDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.NodeDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@AllArgsConstructor
public class NodeDSLMapperImpl implements NodeDSLMapper {
    private final PanelAttributesDSLMapper panelAttributesDSLMapper;
    private final CoordinatesDSLMapper coordinatesDSLMapper;
    private final PropertiesDSLMapper propertiesDSLMapper;

    @Override
    public NodeDSL toDSL(Node node) throws DSLException {
        if (node == null) {
            return null;
        }

        MetadataDSL metadataDSL = new MetadataDSL();
        metadataDSL.setPanelAttributes(panelAttributesDSLMapper.toDSL(node.getUmletAttributes()));
        metadataDSL.setAdditionalAttributes(node.getGeneratedAttributes());
        metadataDSL.setCoordinates(coordinatesDSLMapper.toDSL(node.getUmletPosition()));

        NodeDSL nodeDSL = new NodeDSL();
        nodeDSL.setId(node.getId());
        nodeDSL.setElementType(node.getElementType());
        nodeDSL.setText(node.getDescription());
        nodeDSL.setMcmType(node.getMcmType());
        nodeDSL.setMetadata(metadataDSL);
        nodeDSL.setProperties(propertiesDSLMapper.toDSL(node.getMcmAttributes()));
        if (node.getMcmAttributes() != null && node.getMcmAttributes().containsKey(AttributeKeys.TAGS) && node.getMcmAttributes().get(AttributeKeys.TAGS) instanceof Set) {
            nodeDSL.setTags((Set<String>) node.getMcmAttributes().get(AttributeKeys.TAGS));
        }

        return nodeDSL;
    }

    @Override
    public Node fromDSL(NodeDSL nodeDSL) throws DSLException {
        if (nodeDSL == null) {
            return null;
        }

        if (nodeDSL.getMetadata() == null) {
            throw new DSLException("Metadata of the node " + nodeDSL.getId() + " cannot be null");
        }

        Node node = new Node();
        node.setId(nodeDSL.getId());
        node.setElementType(nodeDSL.getElementType());
        node.setDescription(nodeDSL.getText());
        node.setMcmType(nodeDSL.getMcmType());
        node.setUmletPosition(coordinatesDSLMapper.fromDSL(nodeDSL.getMetadata().getCoordinates()));
        node.setGeneratedAttributes(nodeDSL.getMetadata().getAdditionalAttributes());
        node.setUmletAttributes(panelAttributesDSLMapper.fromDSL(nodeDSL.getMetadata().getPanelAttributes()));

        Map<String, Object> mcmAttributes = propertiesDSLMapper.fromDSL(nodeDSL.getProperties());
        Set<String> tags = nodeDSL.getTags();

        // TODO: since tags are also dedicated prop in Neo4j entities, consider having a dedicated field in domain model
        if (tags != null) {
            if (mcmAttributes == null) {
                mcmAttributes = new HashMap<>();
            }
            mcmAttributes.put(AttributeKeys.TAGS, tags);
            node.setMcmAttributes(mcmAttributes);
        }

        return node;
    }
}
