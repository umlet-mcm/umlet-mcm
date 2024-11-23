package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import at.ac.tuwien.model.change.management.server.dto.ModelDTO;
import at.ac.tuwien.model.change.management.server.dto.NodeDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ModelDtoMapperTest {

    private final ModelDtoMapper mapper = Mappers.getMapper(ModelDtoMapper.class);

    @Test
    void testToDto() {
        Node node = new Node();
        Model model = new Model();

        model.setNodes(Set.of(node));
        model.setId("1");

        ModelDTO modelDTO = mapper.toDto(model);

        assertNotNull(modelDTO);
        assertEquals(model.getId(), modelDTO.id());
        assertEquals(1, modelDTO.nodes().size());
    }

    @Test
    void testFromDto() {
        UMLetPosition umLetPosition = new UMLetPosition(1, 1, 1, 1);
        NodeDTO nodeDTO = new NodeDTO("1", "text", Set.of(), "type", Map.of(), Set.of(), umLetPosition);
        ModelDTO modelDto = new ModelDTO("1", Set.of(nodeDTO));

        Model model = mapper.fromDto(modelDto);

        assertNotNull(model);
        assertEquals(modelDto.id(), model.getId());
        assertEquals(1, model.getNodes().size());
    }
}
