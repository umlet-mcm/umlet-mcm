package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelDtoMapperTest extends MapperTest {

    private ModelDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ModelDtoMapper.class);
    }

    @Test
    void testToDto() {
        Model model = new Model();
        model.setNodes(Set.of(new Node()));
        model.setId("model-123");
        model.setTags(List.of("tag1", "tag2"));
        model.setOriginalText("Original text of the model");
        model.setTitle("Model Title");
        model.setDescription("Model Description");
        model.setMcmAttributes(new LinkedHashMap<>());

        ModelDTO dto = mapper.toDto(model);

        assertNotNull(dto);
        assertEquals(model.getId(), dto.id());
        assertEquals(model.getTags(), dto.tags());
        assertEquals(model.getOriginalText(), dto.originalText());
        assertEquals(model.getTitle(), dto.title());
        assertEquals(model.getDescription(), dto.description());
        assertNotNull(dto.mcmAttributes());
        assertTrue(dto.mcmAttributes().isEmpty());
        assertEquals(model.getNodes().size(), dto.nodes().size());
    }

    @Test
    void testFromDto() {
        NodeDTO nodeDTO = getNodeDTO(Set.of(), "node-123");
        ModelDTO modelDTO = getModelDTO(Set.of(nodeDTO), "model-123");

        Model model = mapper.fromDto(modelDTO);

        assertNotNull(model);
        assertEquals(modelDTO.id(), model.getId());
        assertEquals(modelDTO.tags(), model.getTags());
        assertEquals(modelDTO.originalText(), model.getOriginalText());
        assertEquals(modelDTO.title(), model.getTitle());
        assertEquals(modelDTO.description(), model.getDescription());
        assertNotNull(model.getMcmAttributes());
        assertFalse(model.getMcmAttributes().isEmpty());
        assertNotNull(model.getNodes());
        assertFalse(model.getNodes().isEmpty());
    }
}
