package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.dto.RelationDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NodeDtoMapperTest {

    private final NodeDtoMapper mapper = Mappers.getMapper(NodeDtoMapper.class);

    @Test
    void testToDto() {
        Node node = new Node();

        node.setId("1");
        node.setType("type");
        node.setText("text");
        node.setLabels(Set.of("label"));
        node.setRelations(Set.of(new Relation()));
        node.setProperties(Map.of("key", new Object()));
        node.setUmletPosition(new UMLetPosition(1, 1, 1, 1));

        NodeDTO nodeDTO = mapper.toDto(node);

        assertNotNull(nodeDTO);
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
        UMLetPosition umLetPosition = new UMLetPosition(1, 1, 1, 1);

        NodeDTO nodeDTO = new NodeDTO(
                "1",
                "text",
                Set.of(),
                "type",
                Map.of("key", "value"),
                Set.of("label"),
                umLetPosition
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
