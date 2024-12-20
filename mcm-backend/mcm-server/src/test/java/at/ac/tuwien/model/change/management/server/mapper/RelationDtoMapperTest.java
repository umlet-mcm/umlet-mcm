package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.dto.RelationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RelationDtoMapperTest extends MapperTest {

    private RelationDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RelationDtoMapper.class);
    }

    @Test
    void testToDto() {
        var targetNode = new Node();
        targetNode.setId("c4144490-b60b-4283-b8a1-51cc631c3874");

        Relation relation = getRelation(targetNode, "relation-123", "6c1d704c-7535-4020-a8a4-8ef39b9cd22e");
        RelationDTO dto = mapper.toDto(relation);

        assertNotNull(dto);
        assertEquals(relation.getId(), dto.id());
        assertEquals(Optional.ofNullable(relation.getTarget()).map(Node::getId).orElse(null), dto.target());
        assertEquals(relation.getType(), dto.type());
        assertEquals(relation.getTags(), dto.tags());
        assertEquals(relation.getTitle(), dto.title());
        assertEquals(relation.getMcmModel(), dto.mcmModel());
        assertEquals(relation.getMcmModelId(), dto.mcmModelId());
        assertEquals(relation.getUmletAttributes(), dto.umletAttributes());
        assertEquals(relation.getOriginalText(), dto.originalText());
        assertEquals(relation.getPprType(), dto.pprType());
        assertEquals(relation.getUmletPosition().getX(), dto.umletPosition().x());
        assertEquals(relation.getRelativeEndPoint().getAbsX(), dto.relativeEndPoint().absX());
        assertEquals(relation.getRelativeStartPoint().getAbsX(), dto.relativeStartPoint().absX());
        assertEquals(relation.getRelativeMidPoints().size(), dto.relativeMidPoints().size());
        assertEquals(relation.getStartPoint().x(), dto.startPoint().x());
        assertEquals(relation.getEndPoint().x(), dto.endPoint().x());
    }

    @Test
    void testFromDTO() {
        NodeDTO tgtDTO = getNodeDTO(new HashSet<>(), "node-123", "c4144490-b60b-4283-b8a1-51cc631c3874");
        RelationDTO relationDTO = getRelationDTO(tgtDTO, "relation-123", "6c1d704c-7535-4020-a8a4-8ef39b9cd22e");
        tgtDTO.relations().add(relationDTO);

        Relation relation = mapper.fromDto(relationDTO);

        assertNotNull(relation);
        assertEquals(relationDTO.id(), relation.getId());
        assertEquals(relationDTO.type(), relation.getType());
        assertEquals(relationDTO.tags(), relation.getTags());
        assertEquals(relationDTO.title(), relation.getTitle());
        assertEquals(relationDTO.mcmModel(), relation.getMcmModel());
        assertEquals(relationDTO.mcmModelId(), relation.getMcmModelId());
        assertEquals(relationDTO.mcmAttributes(), relation.getUmletAttributes());
        assertEquals(relationDTO.originalText(), relation.getOriginalText());
        assertEquals(relationDTO.pprType(), relation.getPprType());
        assertEquals(relationDTO.umletPosition().x(), relation.getUmletPosition().getX());
        assertEquals(relationDTO.relativeEndPoint().offsetX(), relation.getRelativeEndPoint().getOffsetX());
        assertEquals(relationDTO.relativeStartPoint().offsetX(), relation.getRelativeStartPoint().getOffsetX());
        assertEquals(relationDTO.relativeMidPoints().size(), relation.getRelativeMidPoints().size());
        assertEquals(relationDTO.startPoint().x(), relation.getStartPoint().x());
        assertEquals(relationDTO.endPoint().x(), relation.getEndPoint().x());
    }
}
