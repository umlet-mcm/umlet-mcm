package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import at.ac.tuwien.model.change.management.server.dto.RelationDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = {MapperTestConfig.class})
public class NodeDtoMapperTest extends MapperTest {

    @Autowired
    private NodeDtoMapper mapper;

    @Test
    void testToDto() {
        Node node = getNode(Set.of(new Relation()), "node-123", "c4144490-b60b-4283-b8a1-51cc631c3874");

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
        assertEquals(node.getMcmModelId(), dto.mcmModelId());
        assertEquals(node.getGeneratedAttributes(), dto.generatedAttributes());
        assertEquals(node.getUmletAttributes(), dto.umletAttributes());
    }

    @Test
    void testFromDto() {
        var modelId = "c4144490-b60b-4283-b8a1-51cc631c3874";
        NodeDTO nodeDTO = getNodeDTO(new HashSet<>(), "src-123", modelId);
        RelationDTO relationDTO = getRelationDTO(getNodeDTO(Set.of(), "tgt-123", nodeDTO.mcmModelId()), "relation", modelId);
        nodeDTO.relations().add(relationDTO);

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
        assertEquals(nodeDTO.mcmModelId(), node.getMcmModelId());
        assertEquals(nodeDTO.generatedAttributes(), node.getGeneratedAttributes());
        assertEquals(nodeDTO.umletAttributes(), node.getUmletAttributes());
        assertEquals(nodeDTO.relations().size(), node.getRelations().size());
    }
}
