package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
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
    private final String SRC = "src";
    private final String TGT = "tgt";
    private final String TYPE = "type";
    private final UMLetPosition UMLET_POSITION = new UMLetPosition(1, 1, 1, 1);
    private final Map<String, Object> PROPERTIES = Map.of("key", new Object());
    private final Set<String> LABELS = Set.of("label");
    private final String ID = "1";
    private final Set<RelationDTO> RELATIONS = Set.of();

    @Test
    void testToDto() {
        Node nodeSrc = new Node();
        nodeSrc.setText(SRC);

        Node nodeTgt = new Node();
        nodeTgt.setText(TGT);

        Relation relation = new Relation();
        relation.setSource(nodeSrc);
        relation.setTarget(nodeTgt);
        relation.setType(TYPE);
        relation.setUmletPosition(UMLET_POSITION);

        RelationDTO relationDTO = mapper.toDto(relation);

        assertNotNull(relationDTO);
        assertEquals(relation.getSource().getText(), relationDTO.source().text());
        assertEquals(relation.getTarget().getText(), relationDTO.target().text());
        assertEquals(relation.getType(), relationDTO.type());
        assertEquals(relation.getUmletPosition().x(), relationDTO.umletPosition().x());
    }

    @Test
    void testFromDto() {
        RelationDTO relationDTO = getRelationDTO();

        Relation relation = mapper.fromDto(relationDTO);

        assertNotNull(relation);
        assertEquals(relationDTO.source().text(), relation.getSource().getText());
        assertEquals(relationDTO.target().text(), relation.getTarget().getText());
        assertEquals(relationDTO.type(), relation.getType());
        assertEquals(relationDTO.umletPosition().x(), relation.getUmletPosition().x());
    }

    private RelationDTO getRelationDTO() {
        NodeDTO srcDto = new NodeDTO(
                ID,
                SRC,
                RELATIONS,
                TYPE,
                PROPERTIES,
                LABELS,
                UMLET_POSITION
        );

        NodeDTO tgtDto = new NodeDTO(
                ID,
                TGT,
                RELATIONS,
                TYPE,
                PROPERTIES,
                LABELS,
                UMLET_POSITION
        );

        return new RelationDTO(
                TYPE,
                srcDto,
                tgtDto,
                UMLET_POSITION
        );
    }
}
