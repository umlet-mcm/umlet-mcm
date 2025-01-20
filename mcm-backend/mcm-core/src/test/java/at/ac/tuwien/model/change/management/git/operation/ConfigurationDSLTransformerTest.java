package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.exception.DSLException;
import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.transformer.DSLTransformer;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryWriteException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigurationDSLTransformerTest {

    @Mock
    private DSLTransformer mockTransformer;

    @Mock
    @SuppressWarnings("unused") //actually is needed - just to prevent a nullpointer exception
    private IdGenerator mockIDGenerator;

    @InjectMocks
    ConfigurationDSLTransformerImpl configurationDSLTransformer;

    private static final String TEST_CONFIGURATION_NAME = "testConfiguration";
    private static final String TEST_CONFIGURATION_HASH = "v1.0.0";
    private static final String TEST_CONFIGURATION_VERSION_NAME = "v1.0.0";
    private static final String TEST_CONFIGURATION_VERSION_CUSTOM_NAME = "custom-name";

    @Test
    public void testParseToConfiguration_emptyContents_shouldReturnEmptyConfiguration() throws DSLException {
        when(mockTransformer.parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty))).thenReturn(Collections.emptySet());
        var configuration = configurationDSLTransformer.parseToConfiguration(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        assertNameAndVersionAreNull(configuration);
        Assertions.assertThat(configuration.getModels()).isEmpty();

        verify(mockTransformer).parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty));
    }

    @Test
    public void testParseToConfiguration_emptyContents_nameAndVersionPassed_shouldReturnEmptyConfiguration()
            throws DSLException {
        when(mockTransformer.parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty))).thenReturn(Collections.emptySet());
        var configuration = configurationDSLTransformer.parseToConfiguration(
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersion(TEST_CONFIGURATION_HASH, TEST_CONFIGURATION_VERSION_NAME, TEST_CONFIGURATION_VERSION_CUSTOM_NAME)
        );

        assertNameAndVersionMatchTestConstants(configuration);
        Assertions.assertThat(configuration.getModels()).isEmpty();

        verify(mockTransformer).parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty));
    }

    @Test
    public void testParseToConfiguration_withModel_shouldReturnConfigurationWithModel() throws DSLException {
        var modelDSL = "model";
        var model = new Model();
        when(mockTransformer.parseToModel(modelDSL)).thenReturn(model);
        when(mockTransformer.parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty))).thenReturn(Collections.emptySet());

        var configuration = configurationDSLTransformer.parseToConfiguration(
                Set.of(modelDSL),
                Collections.emptySet(),
                Collections.emptySet(),
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersion(TEST_CONFIGURATION_HASH, TEST_CONFIGURATION_VERSION_NAME, TEST_CONFIGURATION_VERSION_CUSTOM_NAME)
        );

        assertNameAndVersionMatchTestConstants(configuration);
        Assertions.assertThat(configuration.getModels()).containsExactly(model);

        verify(mockTransformer).parseToModel(modelDSL);
        verify(mockTransformer).parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty));
    }

    @Test
    public void testParseToConfiguration_withNode_shouldReturnConfigurationWithNode() throws DSLException {
        var modelDSL = "model";
        var modelID = "modelId";
        var model = new Model();
        model.setId(modelID);

        var nodeDSL = "node";
        var node = new Node();
        node.setMcmModelId(modelID);

        when(mockTransformer.parseToModel(modelDSL)).thenReturn(model);
        when(mockTransformer.parseToNodes(Set.of(nodeDSL), Collections.emptySet())).thenReturn(Set.of(node));

        var configuration = configurationDSLTransformer.parseToConfiguration(
                Set.of(modelDSL),
                Set.of(nodeDSL),
                Collections.emptySet(),
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersion(TEST_CONFIGURATION_HASH, TEST_CONFIGURATION_VERSION_NAME, TEST_CONFIGURATION_VERSION_CUSTOM_NAME)
        );

        assertNameAndVersionMatchTestConstants(configuration);
        Assertions.assertThat(configuration.getModels()).satisfiesExactly(parsedModel -> {
            Assertions.assertThat(parsedModel).isEqualTo(model);
            Assertions.assertThat(parsedModel.getNodes()).containsExactly(node);
        });

        verify(mockTransformer).parseToNodes(Set.of(nodeDSL), Collections.emptySet());
        verify(mockTransformer).parseToModel(modelDSL);
    }

    @Test
    public void testParseToConfiguration_withRelation_shouldReturnConfigurationWithRelation() throws DSLException {
        var modelDSL = "model";
        var modelID = "modelId";
        var model = new Model();
        model.setId(modelID);

        var nodeDSL = "node";
        var node = new Node();
        node.setMcmModelId(modelID);

        var relationDSL = "relation";
        var relation = new Relation();
        node.setRelations(Set.of(relation));

        when(mockTransformer.parseToModel(modelDSL)).thenReturn(model);
        when(mockTransformer.parseToNodes(Set.of(nodeDSL), Set.of(relationDSL))).thenReturn(Set.of(node));

        var configuration = configurationDSLTransformer.parseToConfiguration(
                Set.of(modelDSL),
                Set.of(nodeDSL),
                Set.of(relationDSL),
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersion(TEST_CONFIGURATION_HASH, TEST_CONFIGURATION_VERSION_NAME, TEST_CONFIGURATION_VERSION_CUSTOM_NAME)
        );

        assertNameAndVersionMatchTestConstants(configuration);
        Assertions.assertThat(configuration.getModels()).satisfiesExactly(parsedModel -> {
            Assertions.assertThat(parsedModel).isEqualTo(model);
            Assertions.assertThat(parsedModel.getNodes()).containsExactly(node);
        });

        verify(mockTransformer).parseToNodes(Set.of(nodeDSL), Set.of(relationDSL));
        verify(mockTransformer).parseToModel(modelDSL);
    }

    @Test
    public void testParseToConfiguration_withNodeMissingModelId_shouldThrowRepositoryReadException() throws DSLException {
        var modelDSL = "model";
        var model = new Model();
        model.setId("modelId");

        var nodeDSL = "node";
        when(mockTransformer.parseToModel(modelDSL)).thenReturn(model);
        when(mockTransformer.parseToNodes(Set.of(nodeDSL), Collections.emptySet())).thenReturn(Set.of(new Node()));

        Assertions.assertThatThrownBy(() -> configurationDSLTransformer.parseToConfiguration(
                Set.of(modelDSL),
                Set.of(nodeDSL),
                Collections.emptySet(),
                TEST_CONFIGURATION_NAME,
                TEST_CONFIGURATION_HASH
        )).isInstanceOf(RepositoryReadException.class);

        verify(mockTransformer).parseToModel(modelDSL);
        verify(mockTransformer).parseToNodes(Set.of(nodeDSL), Collections.emptySet());
    }

    @Test
    public void testParseToConfigurationOverload_configurationContentsHelper_shouldReturnConfiguration() throws DSLException {
        var modelDSL = "model";
        var model = new Model();
        when(mockTransformer.parseToModel(modelDSL)).thenReturn(model);
        when(mockTransformer.parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty))).thenReturn(Collections.emptySet());

        var configurationContents = new ConfigurationContents<String, String, String>();
        configurationContents.addModel(modelDSL);

        var configuration = configurationDSLTransformer.parseToConfiguration(configurationContents);

        assertNameAndVersionAreNull(configuration);
        Assertions.assertThat(configuration.getModels()).containsExactly(model);

        verify(mockTransformer).parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty));
        verify(mockTransformer).parseToModel(modelDSL);
    }

    @Test
    public void testParseToConfigurationOverload_configurationContentsHelperWithNameAndVersion_shouldReturnConfiguration() throws DSLException {
        var modelDSL = "model";
        var model = new Model();
        when(mockTransformer.parseToModel(modelDSL)).thenReturn(model);
        when(mockTransformer.parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty))).thenReturn(Collections.emptySet());

        var configurationContents = new ConfigurationContents<String, String, String>();
        configurationContents.addModel(modelDSL);

        var configuration = configurationDSLTransformer.parseToConfiguration(
                configurationContents,
                TEST_CONFIGURATION_NAME,
                new ConfigurationVersion(TEST_CONFIGURATION_HASH, TEST_CONFIGURATION_VERSION_NAME, TEST_CONFIGURATION_VERSION_CUSTOM_NAME)
        );

        assertNameAndVersionMatchTestConstants(configuration);
        Assertions.assertThat(configuration.getModels()).containsExactly(model);

        verify(mockTransformer).parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty));
        verify(mockTransformer).parseToModel(modelDSL);
    }

    @Test
    public void testParseToConfigurationOverload_setsWithoutNameAndVersion_shouldReturnConfiguration() throws DSLException {
        var modelDSL = "model";
        var model = new Model();
        when(mockTransformer.parseToModel(modelDSL)).thenReturn(model);
        when(mockTransformer.parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty))).thenReturn(Collections.emptySet());

        var configuration = configurationDSLTransformer.parseToConfiguration(
                Set.of(modelDSL),
                Collections.emptySet(),
                Collections.emptySet()
        );

        assertNameAndVersionAreNull(configuration);
        Assertions.assertThat(configuration.getModels()).containsExactly(model);

        verify(mockTransformer).parseToNodes(argThat(Set::isEmpty), argThat(Set::isEmpty));
        verify(mockTransformer).parseToModel(modelDSL);
    }

    @Test
    public void testSerializeToDsl_emptyConfiguration_shouldReturnEmptyContents() {
        var configuration = new Configuration();

        var contents = configurationDSLTransformer.serializeToDsl(configuration);

        Assertions.assertThat(contents.getModels()).isEmpty();
        Assertions.assertThat(contents.getNodes()).isEmpty();
        Assertions.assertThat(contents.getRelations()).isEmpty();

        verifyNoInteractions(mockTransformer);
    }

    @Test
    public void testSerializeToDsl_configurationWithModel_shouldReturnModelDSL() throws DSLException {
        var model = new Model();

        var configuration = new Configuration();
        configuration.setModels(Set.of(model));

        when(mockTransformer.parseToModelDSL(model)).thenReturn("model");

        var contents = configurationDSLTransformer.serializeToDsl(configuration);

        Assertions.assertThat(contents.getModels()).satisfiesExactly(modelDSL -> {
            Assertions.assertThat(modelDSL.element()).isEqualTo(model);
            Assertions.assertThat(modelDSL.dsl()).isEqualTo("model");
        });

        verify(mockTransformer).parseToModelDSL(model);
    }

    @Test
    public void testSerializeToDSL_configurationWithNode_shouldReturnNodeDSL() throws DSLException {
        var model = new Model();

        var node = new Node();
        model.setNodes(Set.of(node));

        var configuration = new Configuration();
        configuration.setModels(Set.of(model));

        when(mockTransformer.parseToModelDSL(model)).thenReturn("model");
        when(mockTransformer.parseToNodeDSL(node)).thenReturn("node");

        var contents = configurationDSLTransformer.serializeToDsl(configuration);

        Assertions.assertThat(contents.getModels()).satisfiesExactly(modelDSL -> {
            Assertions.assertThat(modelDSL.element()).isEqualTo(model);
            Assertions.assertThat(modelDSL.dsl()).isEqualTo("model");
        });

        Assertions.assertThat(contents.getNodes()).satisfiesExactly(nodeDSL -> {
            Assertions.assertThat(nodeDSL.element()).isEqualTo(node);
            Assertions.assertThat(nodeDSL.dsl()).isEqualTo("node");
        });

        verify(mockTransformer).parseToModelDSL(model);
        verify(mockTransformer).parseToNodeDSL(node);
    }

    @Test
    public void testSerializeToDSL_configurationWithRelation_shouldReturnRelationDSL() throws DSLException {
        var model = new Model();

        var node = new Node();
        model.setNodes(Set.of(node));

        var relation = new Relation();
        node.setRelations(Set.of(relation));

        var configuration = new Configuration();
        configuration.setModels(Set.of(model));

        when(mockTransformer.parseToModelDSL(model)).thenReturn("model");
        when(mockTransformer.parseToNodeDSL(node)).thenReturn("node");
        when(mockTransformer.parseToRelationDSL(relation, node)).thenReturn("relation");

        var contents = configurationDSLTransformer.serializeToDsl(configuration);

        Assertions.assertThat(contents.getModels()).satisfiesExactly(modelDSL -> {
            Assertions.assertThat(modelDSL.element()).isEqualTo(model);
            Assertions.assertThat(modelDSL.dsl()).isEqualTo("model");
        });

        Assertions.assertThat(contents.getNodes()).satisfiesExactly(nodeDSL -> {
            Assertions.assertThat(nodeDSL.element()).isEqualTo(node);
            Assertions.assertThat(nodeDSL.dsl()).isEqualTo("node");
        });

        Assertions.assertThat(contents.getRelations()).satisfiesExactly(relationDSL -> {
            Assertions.assertThat(relationDSL.element()).isEqualTo(relation);
            Assertions.assertThat(relationDSL.dsl()).isEqualTo("relation");
        });

        verify(mockTransformer).parseToModelDSL(model);
        verify(mockTransformer).parseToNodeDSL(node);
        verify(mockTransformer).parseToRelationDSL(relation, node);
    }

    @Test
    public void testSerializeToDSL_transformerThrowsDSLExceptionWhenParsingModel_shouldThrowRepositoryWriteException() throws DSLException {
        var model = new Model();

        var configuration = new Configuration();
        configuration.setModels(Set.of(model));

        when(mockTransformer.parseToModelDSL(model)).thenThrow(new DSLException(""));

        Assertions.assertThatThrownBy(() -> configurationDSLTransformer.serializeToDsl(configuration))
                .isInstanceOf(RepositoryWriteException.class);
        verify(mockTransformer).parseToModelDSL(model);
    }

    @Test
    public void testSerializeToDSL_transformerThrowsDSLExceptionWhenParsingNode_shouldThrowRepositoryWriteException() throws DSLException {
        var model = new Model();

        var node = new Node();
        model.setNodes(Set.of(node));

        var configuration = new Configuration();
        configuration.setModels(Set.of(model));

        when(mockTransformer.parseToModelDSL(model)).thenReturn("model");
        when(mockTransformer.parseToNodeDSL(node)).thenThrow(new DSLException(""));

        Assertions.assertThatThrownBy(() -> configurationDSLTransformer.serializeToDsl(configuration))
                .isInstanceOf(RepositoryWriteException.class);
        verify(mockTransformer).parseToModelDSL(model);
        verify(mockTransformer).parseToNodeDSL(node);
    }

    @Test
    public void testSerializeToDSL_transformerThrowsDSLExceptionWhenParsingRelation_shouldThrowRepositoryWriteException() throws DSLException {
        var model = new Model();

        var node = new Node();
        model.setNodes(Set.of(node));

        var relation = new Relation();
        node.setRelations(Set.of(relation));

        var configuration = new Configuration();
        configuration.setModels(Set.of(model));

        when(mockTransformer.parseToModelDSL(model)).thenReturn("model");
        when(mockTransformer.parseToNodeDSL(node)).thenReturn("node");
        when(mockTransformer.parseToRelationDSL(relation, node)).thenThrow(new DSLException(""));

        Assertions.assertThatThrownBy(() -> configurationDSLTransformer.serializeToDsl(configuration))
                .isInstanceOf(RepositoryWriteException.class);
        verify(mockTransformer).parseToModelDSL(model);
        verify(mockTransformer).parseToNodeDSL(node);
        verify(mockTransformer).parseToRelationDSL(relation, node);
    }
    
    private void assertNameAndVersionAreNull(Configuration configuration) {
        Assertions.assertThat(configuration.getName()).isNull();
        Assertions.assertThat(configuration.getVersionHash()).isNull();
    }

    private void assertNameAndVersionMatchTestConstants(Configuration configuration) {
        Assertions.assertThat(configuration.getName()).isEqualTo(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(configuration.getVersionHash()).isEqualTo(TEST_CONFIGURATION_HASH);
        Assertions.assertThat(configuration.getVersionName()).isEqualTo(TEST_CONFIGURATION_VERSION_NAME);
        Assertions.assertThat(configuration.getVersionCustomName()).isEqualTo(TEST_CONFIGURATION_VERSION_CUSTOM_NAME);
    }
}
