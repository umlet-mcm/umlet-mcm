package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.server.dto.*;
import at.ac.tuwien.model.change.management.server.testutil.DtoGen;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ConfigurationDtoMapperImpl.class, ModelDtoMapperImpl.class, NodeDtoMapperImpl.class, RelationDtoMapperImpl.class})
class ConfigurationDtoMapperTest extends MapperTest {

    @Autowired
    private ConfigurationDtoMapper mapper;

    @Test
    void testToDto() {
        Configuration configuration = new Configuration();
        configuration.setName("Test Config");
        configuration.setVersion("1.0");
        configuration.setModels(Set.of(new Model()));

        ConfigurationDTO dto = mapper.toDto(configuration);

        assertNotNull(dto);
        assertEquals(configuration.getName(), dto.name());
        assertEquals(configuration.getVersion(), dto.version());
        assertNotNull(dto.models());
        assertFalse(dto.models().isEmpty());
    }

    @Test
    void testFromDto() {
        ModelDTO modelDTO = getModelDTO(Set.of(), "model-123");

        ConfigurationDTO dto = new ConfigurationDTO(
                "Test Config",
                "1.0",
                Set.of(modelDTO)
        );

        Configuration configuration = mapper.fromDto(dto);

        assertNotNull(configuration);
        assertEquals("Test Config", configuration.getName());
        assertEquals("1.0", configuration.getVersion());
        assertNotNull(configuration.getModels());
        assertFalse(configuration.getModels().isEmpty());
    }


    @Test
    void testFromLargeDto() {
        ConfigurationDTO dto = DtoGen.generateRandomizedConfigurationDTO("test", 2, 10, 5);
        Configuration configuration = mapper.fromDto(dto);
        Assertions.assertThat(configuration)
                .usingRecursiveComparison()
                .ignoringFields(
                        "models.nodes.relations.target",
                        "models.nodes.relations.relativeStartPoint",
                        "models.nodes.relations.relativeMidPoints",
                        "models.nodes.relations.relativeEndPoint"
                )
                .isEqualTo(dto);

        var dtoTargetIDs = dto.models().stream()
                .flatMap(modelDTO -> modelDTO.nodes().stream())
                .flatMap(nodeDTO -> nodeDTO.relations().stream())
                .map(RelationDTO::target)
                .toList();

        var entityTargetIDs = configuration.getModels().stream()
                .flatMap(model -> model.getNodes().stream())
                .flatMap(node -> node.getRelations().stream())
                .map(Relation::getTarget)
                .filter(Objects::nonNull)
                .map(BaseAttributes::getId)
                .toList();

        Assertions.assertThat(entityTargetIDs).containsExactlyInAnyOrderElementsOf(dtoTargetIDs);
    }

    @Test
    void testFromDto_relationWithoutTarget_shouldSucceed() {
        ConfigurationDTO dto = DtoGen.generateRandomizedConfigurationDTO("test", 1, 1, 0);
        var nodeDto = dto.models().iterator().next().nodes().iterator().next();
        var relationDto = getRelationDTO(null, "relation-123", nodeDto.id());
        nodeDto.relations().add(relationDto);
        Assertions.assertThatCode(() -> mapper.fromDto(dto))
                .doesNotThrowAnyException();
    }
}
