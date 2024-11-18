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

public class RelationDtoMapperTest {

    private final RelationDtoMapper mapper = Mappers.getMapper(RelationDtoMapper.class);

    @Test
    void testToDto() {
        Node nodeSrc = new Node();
        nodeSrc.setText("src");

        Node nodeTgt = new Node();
        nodeTgt.setText("tgt");

        Relation relation = new Relation();
        relation.setSource(nodeSrc);
        relation.setTarget(nodeTgt);
        relation.setType("type");
        relation.setUmletPosition(new UMLetPosition(1, 1, 1, 1));

        RelationDTO relationDTO = mapper.toDto(relation);

        assertNotNull(relationDTO);
        assertEquals(relation.getSource().getText(), relationDTO.source().text());
        assertEquals(relation.getTarget().getText(), relationDTO.target().text());
        assertEquals(relation.getType(), relationDTO.type());
        assertEquals(relation.getUmletPosition().x(), relationDTO.umletPosition().x());
    }

    @Test
    void testFromDto() {
        NodeDTO srcDto = new NodeDTO(
                "1",
                "src",
                Set.of(),
                "type",
                Map.of("key", "value"),
                Set.of("label"),
                new UMLetPosition(1, 1, 1, 1)
        );

        NodeDTO tgtDto = new NodeDTO(
                "1",
                "tgt",
                Set.of(),
                "type",
                Map.of("key", "value"),
                Set.of("label"),
                new UMLetPosition(1, 1, 1, 1)
        );

        RelationDTO relationDTO = new RelationDTO(
                "type",
                srcDto,
                tgtDto,
                new UMLetPosition(1, 1, 1, 1)
        );

        Relation relation = mapper.fromDto(relationDTO);


        assertNotNull(relation);
        assertEquals(relationDTO.source().text(), relation.getSource().getText());
        assertEquals(relationDTO.target().text(), relation.getTarget().getText());
        assertEquals(relationDTO.type(), relation.getType());
        assertEquals(relationDTO.umletPosition().x(), relation.getUmletPosition().x());
    }
}
