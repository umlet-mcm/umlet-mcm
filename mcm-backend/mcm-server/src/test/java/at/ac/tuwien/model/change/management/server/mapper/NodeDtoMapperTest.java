package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.dto.RelationDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {MapperTestConfig.class})
public class NodeDtoMapperTest extends MapperTest {

    @Autowired
    private NodeDtoMapper mapper;

    @Test
    void testToDto() {
        Node node = getNode(Set.of(new Relation()), "node-123");

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        NodeDTO dto = mapper.toDto(node, context);

        assertNotNull(dto);
        assertEquals(node.getId(), dto.id());
        assertEquals(node.getTags(), dto.tags());
        assertEquals(node.getOriginalText(), dto.originalText());
        assertEquals(node.getTitle(), dto.title());
        assertEquals(node.getDescription(), dto.description());
        assertEquals(node.getMcmAttributes(), dto.mcmAttributes());
        assertEquals(node.getPprType(), dto.pprType());
        assertEquals(node.getElementType(), dto.elementType());
        assertEquals(node.getRelations().size(), dto.relations().size());
        assertEquals(node.getUmletPosition().getX(), dto.umletPosition().x());
        assertEquals(node.getMcmModel(), dto.mcmModel());
        assertEquals(node.getGeneratedAttributes(), dto.generatedAttributes());
        assertEquals(node.getUmletAttributes(), dto.umletAttributes());
    }

    @Test
    void testFromDto() {
        RelationDTO relationDTO = getRelationDTO(getNodeDTO(Set.of(), "tgt-123"), "relation");
        NodeDTO nodeDTO = getNodeDTO(Set.of(relationDTO), "src-123");

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        Node node = mapper.fromDto(nodeDTO, context);

        assertNotNull(node);
        assertEquals(nodeDTO.id(), node.getId());
        assertEquals(nodeDTO.tags(), node.getTags());
        assertEquals(nodeDTO.originalText(), node.getOriginalText());
        assertEquals(nodeDTO.title(), node.getTitle());
        assertEquals(nodeDTO.description(), node.getDescription());
        assertEquals(nodeDTO.mcmAttributes(), node.getMcmAttributes());
        assertEquals(nodeDTO.pprType(), node.getPprType());
        assertEquals(nodeDTO.elementType(), node.getElementType());
        assertEquals(nodeDTO.relations().size(), node.getRelations().size());
        assertEquals(nodeDTO.umletPosition().x(), node.getUmletPosition().getX());
        assertEquals(nodeDTO.mcmModel(), node.getMcmModel());
        assertEquals(nodeDTO.generatedAttributes(), node.getGeneratedAttributes());
        assertEquals(nodeDTO.umletAttributes(), node.getUmletAttributes());
        assertEquals(nodeDTO.relations().size(), node.getRelations().size());
    }

    @Test
    void testFromDTOToDTO_withCycle() {
        Relation srcToTgt = getRelation(null, "relation-123");
        Relation tgtToSrc = getRelation(null, "relation-456");

        Node src = getNode(Set.of(), "src-123");
        Node tgt = getNode(Set.of(), "tgt-123");

        srcToTgt.setTarget(tgt);
        src.setRelations(Set.of(srcToTgt));

        tgtToSrc.setTarget(src);
        tgt.setRelations(Set.of(tgtToSrc));

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        NodeDTO nodeDTO = mapper.toDto(src, context);

        assertNotNull(nodeDTO);
        assertEquals(src.getId(), nodeDTO.id());

        RelationDTO srcToTgtDTO = nodeDTO.relations().iterator().next();
        assertEquals(srcToTgt.getId(), srcToTgtDTO.id());

        NodeDTO tgtDTO = srcToTgtDTO.target();
        assertNotNull(tgtDTO);
        assertEquals(tgt.getId(), tgtDTO.id());
        assertEquals(1, tgtDTO.relations().size());

        RelationDTO tgtToSrcDTO = tgtDTO.relations().iterator().next();
        assertEquals(tgtToSrc.getId(), tgtToSrcDTO.id());
        assertEquals(src.getId(), tgtToSrcDTO.target().id());

        Node node = mapper.fromDto(nodeDTO, context);

        assertNotNull(node);
        assertEquals(src.getId(), node.getId());
        assertEquals(1, node.getRelations().size());

        Relation srcToTgtRelation = node.getRelations().iterator().next();
        assertEquals(srcToTgt.getId(), srcToTgtRelation.getId());

        Node tgtNode = srcToTgtRelation.getTarget();
        assertNotNull(tgtNode);
        assertEquals(tgt.getId(), tgtNode.getId());
        assertEquals(1, tgtNode.getRelations().size());

        Relation tgtToSrcRelation = tgtNode.getRelations().iterator().next();
        assertEquals(tgtToSrc.getId(), tgtToSrcRelation.getId());

        Node srcNode = tgtToSrcRelation.getTarget();
        assertNotNull(srcNode);
        assertEquals(src.getId(), srcNode.getId());
        assertEquals(1, srcNode.getRelations().size());
    }
}
