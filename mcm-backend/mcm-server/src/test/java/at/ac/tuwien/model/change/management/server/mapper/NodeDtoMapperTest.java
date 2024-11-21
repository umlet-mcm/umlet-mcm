package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NodeDtoMapperTest {

    private final NodeDtoMapper mapper = Mappers.getMapper(NodeDtoMapper.class);
    private final String ID = "1";
    private final String TYPE = "type";
    private final String TEXT = "test";
    private final Set<String> LABELS = Set.of("label");
    private final UMLetPosition UMLET_POSITION = new UMLetPosition(1, 1, 1, 1);
    private final Map<String, Object> PROPERTIES = Map.of("key", new Object());

    @Test
    void testToDto() {
        Node node = new Node();

        node.setId(ID);
        node.setType(TYPE);
        node.setText(TEXT);
        node.setLabels(LABELS);
        node.setRelations(Set.of(new Relation()));
        node.setProperties(PROPERTIES);
        node.setUmletPosition(UMLET_POSITION);

        NodeDTO nodeDTO = mapper.toDto(node);

        assertNotNull(nodeDTO);
        assertEquals(node.getId(), nodeDTO.id());
        assertEquals(node.getType(), nodeDTO.type());
        assertEquals(node.getText(), nodeDTO.text());
        assertEquals(node.getLabels(), nodeDTO.labels());
        assertEquals(1, nodeDTO.relations().size());
        assertEquals(node.getProperties(), nodeDTO.properties());
    }

    @Test
    void testFromDto() {
        NodeDTO nodeDTO = new NodeDTO(
                ID,
                TEXT,
                Set.of(),
                TYPE,
                PROPERTIES,
                LABELS,
                UMLET_POSITION
        );

        Node node = mapper.fromDto(nodeDTO);

        assertNotNull(node);
        assertEquals(nodeDTO.id(), node.getId());
        assertEquals(nodeDTO.type(), node.getType());
        assertEquals(nodeDTO.text(), node.getText());
        assertEquals(nodeDTO.labels(), node.getLabels());
        assertEquals(0, node.getRelations().size());
        assertEquals(nodeDTO.properties(), node.getProperties());
    }
}
