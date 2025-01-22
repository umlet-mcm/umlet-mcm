package at.ac.tuwien.model.change.management.git.operation;

import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryReadException;
import at.ac.tuwien.model.change.management.git.infrastructure.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ConfigurationRepositoryActionsTest {
    private static final String NODES_DIRECTORY = "nodes";
    private static final String RELATIONS_DIRECTORY = "relations";
    private static final String MODELS_DIRECTORY = "models";
    private static final String FILE_EXTENSION = ".xml";

    @Mock
    private ManagedRepository mockRepository;

    @Mock
    private ManagedRepositoryVersioning mockVersioning;

    @Mock
    private ConfigurationDSLTransformer mockTransformer;

    @Mock
    private VersionNameGenerator mockVersionNameGenerator;

    @InjectMocks
    private ConfigurationRepositoryActionsImpl configurationRepositoryActions;

    private final Configuration testConfig = new Configuration();

    private final static String TEST_CONFIGURATION_NAME = "test";

    private final static String TEST_CONFIGURATION_HASH = "v1.0.0";

    private final static ConfigurationVersion TEST_CONFIGURATION_VER = new ConfigurationVersion(TEST_CONFIGURATION_HASH, null, null);

    private final static Charset encoding = StandardCharsets.UTF_8;

    @BeforeEach
    public void setup() {
        testConfig.setName(TEST_CONFIGURATION_NAME);
        testConfig.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_HASH, null, null));
        lenient().when(mockRepository.getName()).thenReturn(TEST_CONFIGURATION_NAME);
        lenient().when(mockRepository.getEncoding()).thenReturn(encoding);
        lenient().when(mockRepository.versioning()).thenReturn(mockVersioning);
    }


    @Test
    public void testWriteConfigurationToRepository_emptyConfiguration_shouldReturnEmptyList() {
        when(mockTransformer.serializeToDsl(testConfig)).thenReturn(new ConfigurationContents<>());
        when(mockRepository.writeRepositoryFiles(argThat(Collection::isEmpty))).thenReturn(Collections.emptyList());

        var writtenFiles = configurationRepositoryActions.writeConfigurationToWorkingDirectory(mockRepository, testConfig);

        Assertions.assertThat(writtenFiles).isEmpty();
        verify(mockRepository).writeRepositoryFiles(argThat(Collection::isEmpty));
        verify(mockTransformer).serializeToDsl(testConfig);
    }

    @Test
    public void testWriteConfigurationToRepository_configurationWithOneElement_shouldReturnOnePath() {
        var contents = getContents(new Model());
        when(mockTransformer.serializeToDsl(testConfig)).thenReturn(contents);
        when(mockRepository.writeRepositoryFiles(argThat(col -> col.size() == 1))).thenReturn(getPaths("model"));

        var writtenFiles = configurationRepositoryActions.writeConfigurationToWorkingDirectory(mockRepository, testConfig);

        Assertions.assertThat(writtenFiles).hasSize(1);
        verify(mockRepository).writeRepositoryFiles(argThat(col -> col.size() == 1));
        verify(mockTransformer).serializeToDsl(testConfig);
    }

    @Test
    public void testWriteConfigurationToRepository_configurationWithFiveElements_shouldReturnFivePaths() {
        var contents = getContents(new Model(), new Node(), new Node(), new Node(), new Relation());
        when(mockTransformer.serializeToDsl(testConfig)).thenReturn(contents);
        when(mockRepository.writeRepositoryFiles(argThat(col -> col.size() == 5))).thenReturn(
                getPaths("model", "node1", "node2", "relation1", "relation2")
        );

        var writtenFiles = configurationRepositoryActions.writeConfigurationToWorkingDirectory(mockRepository, testConfig);

        Assertions.assertThat(writtenFiles).hasSize(5);
        verify(mockRepository).writeRepositoryFiles(argThat(col -> col.size() == 5));
        verify(mockTransformer).serializeToDsl(testConfig);
    }

    @Test
    public void testReadCurrentConfigurationVersion_emptyRepository_shouldReturnEmptyConfiguration() {
        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, true);
        var optionalConfiguration = configurationRepositoryActions.readCurrentConfigurationVersion(mockRepository);

        Assertions.assertThat(optionalConfiguration).hasValueSatisfying(configuration -> {
            assertNameAndVersionMatchTestConstants(configuration);
            Assertions.assertThat(configuration.getModels()).isEmpty();
        });

        verify(mockRepository).getCurrentRepositoryVersion();
        verify(mockTransformer).parseToConfiguration(argThat(this::contentsEmpty), eq(TEST_CONFIGURATION_NAME), eq(TEST_CONFIGURATION_VER));
    }

    @Test
    public void testReadCurrentConfigurationVersion_repositoryWithModel_shouldReturnConfigurationWithModel() {
        var model = new Model();

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, true, model);
        var optionalConfiguration = configurationRepositoryActions.readCurrentConfigurationVersion(mockRepository);

        Assertions.assertThat(optionalConfiguration).hasValueSatisfying(configuration -> {
            assertNameAndVersionMatchTestConstants(configuration);
            Assertions.assertThat(configuration.getModels()).containsExactly(model);
        });

        verify(mockRepository).getCurrentRepositoryVersion();
        verify(mockTransformer).parseToConfiguration(argThat(c -> contentsSize(c, 1)), eq(TEST_CONFIGURATION_NAME), eq(TEST_CONFIGURATION_VER));
    }

    @Test
    public void testReadCurrentConfiguration_repositoryWithNode_shouldReturnConfigurationWithNode() {
        var node = new Node();
        var model = getModel(node);

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, true, model, node);
        var optionalConfiguration = configurationRepositoryActions.readCurrentConfigurationVersion(mockRepository);

        Assertions.assertThat(optionalConfiguration).hasValueSatisfying(configuration -> {
            assertNameAndVersionMatchTestConstants(configuration);
            Assertions.assertThat(configuration.getModels()).containsExactly(model);
            Assertions.assertThat(configuration.getModels()).flatExtracting(Model::getNodes).containsExactly(node);
        });

        verify(mockRepository).getCurrentRepositoryVersion();
        verify(mockTransformer).parseToConfiguration(argThat(c -> contentsSize(c, 2)), eq(TEST_CONFIGURATION_NAME), eq(TEST_CONFIGURATION_VER));
    }

    @Test
    public void testReadCurrentConfiguration_repositoryWithRelation_shouldReturnConfigurationWithRelation() {
        var relation = new Relation();
        var node = getNode(relation);
        var model = getModel(node);

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, true, model, node, relation);
        var optionalConfiguration = configurationRepositoryActions.readCurrentConfigurationVersion(mockRepository);

        Assertions.assertThat(optionalConfiguration).hasValueSatisfying(configuration -> {
            assertNameAndVersionMatchTestConstants(configuration);
            Assertions.assertThat(configuration.getModels()).containsExactly(model);
            Assertions.assertThat(configuration.getModels()).flatExtracting(Model::getNodes).containsExactly(node);
            Assertions.assertThat(configuration.getModels()).flatExtracting(Model::getNodes).flatExtracting(Node::getRelations).containsExactly(relation);
        });

        verify(mockRepository).getCurrentRepositoryVersion();
        verify(mockTransformer).parseToConfiguration(argThat(c -> contentsSize(c, 3)), eq(TEST_CONFIGURATION_NAME), eq(TEST_CONFIGURATION_VER));
    }

    @Test
    public void testReadCurrentConfiguration_repositoryWithFiveElements_shouldReturnConfigurationWithFiveElements() {
        var relation = new Relation();
        var nodes = new Node[]{new Node(), new Node(), getNode(relation)};
        var model = getModel(nodes);

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, true,
                model, nodes[0], nodes[1], nodes[2], relation);
        var optionalConfiguration = configurationRepositoryActions.readCurrentConfigurationVersion(mockRepository);

        Assertions.assertThat(optionalConfiguration).hasValueSatisfying(configuration -> {
            assertNameAndVersionMatchTestConstants(configuration);
            Assertions.assertThat(configuration.getModels()).containsExactly(model);
            Assertions.assertThat(configuration.getModels()).flatExtracting(Model::getNodes).containsExactlyInAnyOrder(nodes);
            Assertions.assertThat(configuration.getModels()).flatExtracting(Model::getNodes).flatExtracting(Node::getRelations).containsExactly(relation);
        });

        verify(mockRepository).getCurrentRepositoryVersion();
        verify(mockTransformer).parseToConfiguration(argThat(c -> contentsSize(c, 5)), eq(TEST_CONFIGURATION_NAME), eq(TEST_CONFIGURATION_VER));
    }

    @Test
    public void testReadCurrentConfigurationVersion_nonExistingVersion_shouldReturnEmptyOptional() {
        when(mockRepository.getCurrentRepositoryVersion()).thenReturn(Optional.empty());
        var optionalConfiguration = configurationRepositoryActions.readCurrentConfigurationVersion(mockRepository);

        Assertions.assertThat(optionalConfiguration).isEmpty();
        verify(mockRepository).getCurrentRepositoryVersion();
        verifyNoInteractions(mockTransformer);
    }

    @Test
    public void testReadConfigurationVersion_emptyVersionedRepository_shouldReturnEmptyList() {
        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, false);
        var optionalConfiguration = configurationRepositoryActions.readConfigurationVersion(mockRepository, TEST_CONFIGURATION_HASH);

        Assertions.assertThat(optionalConfiguration).hasValueSatisfying(configuration -> {
            assertNameAndVersionMatchTestConstants(configuration);
            Assertions.assertThat(configuration.getModels()).isEmpty();
        });

        verify(mockRepository).getRepositoryVersion(TEST_CONFIGURATION_HASH);
        verify(mockTransformer).parseToConfiguration(argThat(this::contentsEmpty), eq(TEST_CONFIGURATION_NAME), eq(TEST_CONFIGURATION_VER));
    }

    @Test
    public void testReadConfigurationVersion_versionWithRelation_shouldReturnConfigurationWithRelation() {
        var relation = new Relation();
        var node = getNode(relation);
        var model = getModel(node);

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, false, model, node, relation);
        var optionalConfiguration = configurationRepositoryActions.readConfigurationVersion(mockRepository, TEST_CONFIGURATION_HASH);

        Assertions.assertThat(optionalConfiguration).hasValueSatisfying(configuration -> {
            assertNameAndVersionMatchTestConstants(configuration);
            Assertions.assertThat(configuration.getModels()).containsExactly(model);
            Assertions.assertThat(configuration.getModels()).flatExtracting(Model::getNodes).containsExactly(node);
            Assertions.assertThat(configuration.getModels()).flatExtracting(Model::getNodes).flatExtracting(Node::getRelations).containsExactly(relation);
        });

        verify(mockRepository).getRepositoryVersion(TEST_CONFIGURATION_HASH);
        verify(mockTransformer).parseToConfiguration(argThat(c -> contentsSize(c, 3)), eq(TEST_CONFIGURATION_NAME), eq(TEST_CONFIGURATION_VER));
    }

    @Test
    public void testReadConfigurationVersion_nonExistingVersion_shouldReturnEmptyOptional() {
        when(mockRepository.getRepositoryVersion(TEST_CONFIGURATION_HASH)).thenReturn(Optional.empty());

        var optionalConfiguration = configurationRepositoryActions.readConfigurationVersion(mockRepository, TEST_CONFIGURATION_HASH);

        Assertions.assertThat(optionalConfiguration).isEmpty();
        verify(mockRepository).getRepositoryVersion(TEST_CONFIGURATION_HASH);
        verifyNoInteractions(mockTransformer);
    }

    @Test
    public void testReadConfigurationVersion_withVersionNames_shouldReturnConfiguration() {
        var generatedName = "v1.0.0";
        var customName = "custom";

        when(mockVersionNameGenerator.isAutoGeneratedVersionName(generatedName)).thenReturn(true);
        when(mockVersionNameGenerator.isAutoGeneratedVersionName(customName)).thenReturn(false);

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, generatedName, customName, false);

        var configuration = configurationRepositoryActions.readConfigurationVersion(mockRepository, TEST_CONFIGURATION_HASH);

        Assertions.assertThat(configuration).isPresent();

        Assertions.assertThat(configuration).hasValueSatisfying(config -> {
            assertNameAndVersionMatchTestConstants(config);
            Assertions.assertThat(config.getVersion().customName()).isEqualTo(customName);
            Assertions.assertThat(config.getVersion().name()).isEqualTo(generatedName);
        });
    }

    @Test
    public void testReadConfigurationVersion_withCustomName_shouldReturnConfiguration() {
        var customName = "custom";

        when(mockVersionNameGenerator.isAutoGeneratedVersionName(customName)).thenReturn(false);

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, null, customName, false);

        var configuration = configurationRepositoryActions.readConfigurationVersion(mockRepository, TEST_CONFIGURATION_HASH);

        Assertions.assertThat(configuration).isPresent();

        Assertions.assertThat(configuration).hasValueSatisfying(config -> {
            assertNameAndVersionMatchTestConstants(config);
            Assertions.assertThat(config.getVersion().customName()).isEqualTo(customName);
        });
    }

    @Test
    public void testReadConfigurationVersion_withVersionName_shouldReturnConfiguration() {
        var generatedName = "v1.0.0";

        when(mockVersionNameGenerator.isAutoGeneratedVersionName(generatedName)).thenReturn(true);

        mockConfigurationRead(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_HASH, generatedName, null, false);

        var configuration = configurationRepositoryActions.readConfigurationVersion(mockRepository, TEST_CONFIGURATION_HASH);

        Assertions.assertThat(configuration).isPresent();

        Assertions.assertThat(configuration).hasValueSatisfying(config -> {
            assertNameAndVersionMatchTestConstants(config);
            Assertions.assertThat(config.getVersion().name()).isEqualTo(generatedName);
        });
    }

    @Test
    public void testClearRepository_shouldDeleteThreePaths() {
        configurationRepositoryActions.clearConfigurationRepository(mockRepository);
        verify(mockRepository).deleteRepositoryFiles(
                Path.of(MODELS_DIRECTORY),
                Path.of(NODES_DIRECTORY),
                Path.of(RELATIONS_DIRECTORY)
        );
    }

    @Test
    public void testCompareConfigurationVersions_noDiffs_includeUnchangedFalse_shouldReturnEmptyList() {
        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false);
        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false);
        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(false), any())).thenReturn(Collections.emptyList());

        var diffs = configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", false);

        Assertions.assertThat(diffs.getModels()).isEmpty();
        Assertions.assertThat(diffs.getNodes()).isEmpty();
        Assertions.assertThat(diffs.getRelations()).isEmpty();

        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
        verify(mockVersioning).compareVersions(eq(version1.id()), eq(version2.id()), eq(false), any());
    }

    @Test
    public void testCompareVersions_versionsWithUnchangedModel_includeUnchangedFalse_shouldReturnEmptyList() {
        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false, new Model());
        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false, new Model());
        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(false), any())).thenReturn(Collections.emptyList());

        var diffs = configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", false);

        Assertions.assertThat(diffs.getModels()).isEmpty();
        Assertions.assertThat(diffs.getNodes()).isEmpty();
        Assertions.assertThat(diffs.getRelations()).isEmpty();

        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
        verify(mockVersioning).compareVersions(eq(version1.id()), eq(version2.id()), eq(false), any());
    }

    @Test
    public void testComparedVersions_versionsWithUnchangedModel_includeUnchangedTrue_shouldReturnUnchangedDiffEntry() {
        var model = new Model();
        model.setId("model");
        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false, model);
        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false, model);
        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any())).thenReturn(Collections.singletonList(
                new ManagedDiffEntry.Unchanged(version2.objects().getFirst(), "content")
        ));

        var diffs = configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", true);

        Assertions.assertThat(diffs.getModels()).satisfiesExactly(modelDiff -> {
            Assertions.assertThat(modelDiff.getElement()).isEqualTo(model);
            Assertions.assertThat(modelDiff.getDiffType()).isEqualTo(ManagedDiffType.UNCHANGED.toString());
        });

        Assertions.assertThat(diffs.getNodes()).isEmpty();
        Assertions.assertThat(diffs.getRelations()).isEmpty();

        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
        verify(mockVersioning).compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any());
    }

    @Test
    public void testComparedVersions_addedModel_shouldReturnAddDiffEntry() {
        var diffString = "diffString";
        var addedModel = new Model();
        addedModel.setId("addedModel");

        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false);
        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false, addedModel);

        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any())).thenReturn(Collections.singletonList(
                new ManagedDiffEntry.Add(version2.objects().getFirst(), diffString)
        ));

        var diffs = configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", true);

        Assertions.assertThat(diffs.getModels()).satisfiesExactly(modelDiff -> {
            Assertions.assertThat(modelDiff.getElement()).isEqualTo(addedModel);
            Assertions.assertThat(modelDiff.getDiffType()).isEqualTo(ManagedDiffType.ADD.toString());
            Assertions.assertThat(modelDiff.getContent()).isEqualTo(diffString);
        });

        Assertions.assertThat(diffs.getNodes()).isEmpty();
        Assertions.assertThat(diffs.getRelations()).isEmpty();

        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
    }

    @Test
    public void testCompareVersions_modifiedModel_shouldReturnModifiedDiffEntry() {
        var diffString = "+modified-original";
        var originalModel = new Model();
        originalModel.setId("originalString");

        var modifiedModel = new Model();
        modifiedModel.setId("modifiedString");

        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false, originalModel);
        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false, modifiedModel);

        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any())).thenReturn(Collections.singletonList(
                new ManagedDiffEntry.Modify(version1.objects().getFirst(), version2.objects().getFirst(), diffString)
        ));

        var diffs = configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", true);

        Assertions.assertThat(diffs.getModels()).satisfiesExactly(modelDiff -> {
            Assertions.assertThat(modelDiff.getElement()).isEqualTo(modifiedModel);
            Assertions.assertThat(modelDiff.getDiffType()).isEqualTo(ManagedDiffType.MODIFY.toString());
            Assertions.assertThat(modelDiff.getContent()).isEqualTo(diffString);
        });
        Assertions.assertThat(diffs.getNodes()).isEmpty();
        Assertions.assertThat(diffs.getRelations()).isEmpty();

        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
    }

    @Test
    public void testCompareVersions_deletedModel_shouldReturnDeletedDiffEntry() {
        var diffString = "diffString";
        var deletedModel = new Model();
        deletedModel.setId("deletedModel");

        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false, deletedModel);
        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false);

        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any())).thenReturn(Collections.singletonList(
                new ManagedDiffEntry.Delete(version1.objects().getFirst(), diffString)
        ));

        var diffs = configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", true);

        Assertions.assertThat(diffs.getModels()).satisfiesExactly(modelDiff -> {
            Assertions.assertThat(modelDiff.getElement()).isEqualTo(deletedModel);
            Assertions.assertThat(modelDiff.getDiffType()).isEqualTo(ManagedDiffType.DELETE.toString());
            Assertions.assertThat(modelDiff.getContent()).isEqualTo(diffString);
        });

        Assertions.assertThat(diffs.getNodes()).isEmpty();
        Assertions.assertThat(diffs.getRelations()).isEmpty();

        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
    }

    @Test
    public void testCompareVersions_multipleChanges_shouldReturnMultipleDiffEntries() {
        var deleteRelation = new Relation();
        deleteRelation.setId("deleteRelation");

        var originalNode = getNode(deleteRelation);
        originalNode.setId("originalNode");

        var unchangingModel = getModel(originalNode);
        unchangingModel.setId("unchangedModel");

        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false, unchangingModel, originalNode, deleteRelation);

        var addedModel = new Model();
        addedModel.setId("addedModel");

        var modifiedNode = new Node();
        modifiedNode.setId(originalNode.getId());

        var unchangedModel = getModel(modifiedNode);
        unchangedModel.setId(unchangingModel.getId());

        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false, unchangedModel, addedModel, modifiedNode);

        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any())).thenReturn(List.of(
                new ManagedDiffEntry.Delete(version1.objects().get(2), "deleteRelation"),
                new ManagedDiffEntry.Add(version2.objects().get(1), "addModel"),
                new ManagedDiffEntry.Modify(version1.objects().get(1), version2.objects().get(2), "modifyNode"),
                new ManagedDiffEntry.Unchanged(version2.objects().get(0), "content")
        ));

        var diffs = configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", true);

        Assertions.assertThat(diffs.getModels()).hasSize(2)
                .anySatisfy(addedModelDiff -> {
                    Assertions.assertThat(addedModelDiff.getDiffType()).isEqualTo(ManagedDiffType.ADD.toString());
                    Assertions.assertThat(addedModelDiff.getElement()).isEqualTo(addedModel);
                    Assertions.assertThat(addedModelDiff.getContent()).isEqualTo("addModel");
                })
                .anySatisfy(unchangedModelDiff -> {
                    Assertions.assertThat(unchangedModelDiff.getDiffType()).isEqualTo(ManagedDiffType.UNCHANGED.toString());
                    Assertions.assertThat(unchangedModelDiff.getElement()).isEqualTo(unchangedModel);
                });

        Assertions.assertThat(diffs.getNodes()).satisfiesExactly(nodeDiff -> {
            Assertions.assertThat(nodeDiff.getDiffType()).isEqualTo(ManagedDiffType.MODIFY.toString());
            Assertions.assertThat(nodeDiff.getElement()).isEqualTo(modifiedNode);
            Assertions.assertThat(nodeDiff.getContent()).isEqualTo("modifyNode");
        });

        Assertions.assertThat(diffs.getRelations()).satisfiesExactly(relationDiff -> {
            Assertions.assertThat(relationDiff.getDiffType()).isEqualTo(ManagedDiffType.DELETE.toString());
            Assertions.assertThat(relationDiff.getElement()).isEqualTo(deleteRelation);
            Assertions.assertThat(relationDiff.getContent()).isEqualTo("deleteRelation");
        });

        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
        verify(mockVersioning).compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any());
    }

    @Test
    public void testCompareVersions_modifiedModelNotFoundInNewConfiguration_shouldThrowRepositoryReadException() {
        var originalModel = new Model();
        originalModel.setId("originalModel");

        var version1 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v1", false, originalModel);
        var version2 = mockConfigurationRead(TEST_CONFIGURATION_NAME, "v2", false);

        when(mockVersioning.compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any())).thenReturn(Collections.singletonList(
                new ManagedDiffEntry.Modify(version1.objects().getFirst(), version1.objects().getFirst(), "diffString")
        ));

        Assertions.assertThatThrownBy(() -> configurationRepositoryActions.compareConfigurationVersions(mockRepository, "v1", "v2", true))
                .isInstanceOf(RepositoryReadException.class)
                .hasMessageContaining("not found in configuration");
        verify(mockRepository).getRepositoryVersion("v1");
        verify(mockRepository).getRepositoryVersion("v2");
        verify(mockVersioning).compareVersions(eq(version1.id()), eq(version2.id()), eq(true), any());
    }

    @Test
    public void testRenameConfiguration_shouldRenameRepository() {
        var newName = "newName";
        configurationRepositoryActions.renameConfigurationRepository(mockRepository, newName);
        verify(mockRepository).renameRepository(newName);
    }

    @Test
    public void testRenameConfiguration_newNameAlreadyExists_shouldThrowRepositoryAlreadyExistsException() {
        var newName = "newName";
        doThrow(new RepositoryAlreadyExistsException(newName)).when(mockRepository).renameRepository(newName);
        Assertions.assertThatThrownBy(() -> configurationRepositoryActions.renameConfigurationRepository(mockRepository, newName))
                .isInstanceOf(RepositoryAlreadyExistsException.class)
                .hasMessageContaining(newName);
    }

    @Test
    public void testGetConfigurationVersionMetadata_nonExistingVersion_shouldReturnConfigurationVersionWithOnlyHashSet() {
        var versionMetadata = configurationRepositoryActions.getConfigurationVersionMetadata(mockRepository, "v1");
        Assertions.assertThat(versionMetadata)
                .extracting(ConfigurationVersion::hash, ConfigurationVersion::name, ConfigurationVersion::customName)
                .containsExactly("v1", null, null);
        verify(mockVersioning).listTagsForCommit("v1");
    }

    @Test
    public void testGetConfigurationVersionMetadata_existingVersionWithUserSuppliedName_shouldReturnMetadata() {
        var testCommit = "testCommit";
        var autoGeneratedName = "v1.0.0";
        var userSuppliedName = "user-generated-name";

        when(mockVersioning.listTagsForCommit(testCommit)).thenReturn(List.of(autoGeneratedName, userSuppliedName));
        when(mockVersionNameGenerator.isAutoGeneratedVersionName(autoGeneratedName)).thenReturn(true);
        when(mockVersionNameGenerator.isAutoGeneratedVersionName(userSuppliedName)).thenReturn(false);

        var versionMetadata = configurationRepositoryActions.getConfigurationVersionMetadata(mockRepository, testCommit);
        Assertions.assertThat(versionMetadata)
                .extracting(ConfigurationVersion::hash, ConfigurationVersion::name, ConfigurationVersion::customName)
                .containsExactly(testCommit, autoGeneratedName, userSuppliedName);
        verify(mockVersioning).listTagsForCommit(testCommit);
    }

    @Test
    public void testGetConfigurationMetadata_existingVersionWithoutUserSuppliedName_shouldReturnMetadata() {
        var testCommit = "testCommit";
        var autoGeneratedName = "v1.0.0";

        when(mockVersioning.listTagsForCommit(testCommit)).thenReturn(List.of(autoGeneratedName));
        when(mockVersionNameGenerator.isAutoGeneratedVersionName(autoGeneratedName)).thenReturn(true);

        var versionMetadata = configurationRepositoryActions.getConfigurationVersionMetadata(mockRepository, testCommit);
        Assertions.assertThat(versionMetadata)
                .extracting(ConfigurationVersion::hash, ConfigurationVersion::name, ConfigurationVersion::customName)
                .containsExactly(testCommit, autoGeneratedName, null);
        verify(mockVersioning).listTagsForCommit(testCommit);
    }

    @Test
    public void testGetMetadataForAllConfigurationVersions_emptyRepository_shouldReturnEmptyList() {
        when(mockVersioning.listVersions()).thenReturn(Collections.emptyList());
        var metadata = configurationRepositoryActions.getMetadataForAllConfigurationVersions(mockRepository);
        Assertions.assertThat(metadata).isEmpty();
        verify(mockVersioning).listVersions();
    }

    @Test
    public void testGetMetadataForAllConfigurationVersions_repositoryWithTwoVersions_shouldReturnTwoMetadataObjects() {
        var version1 = "v1";
        var version2 = "v2";

        when(mockVersioning.listVersions()).thenReturn(List.of(version1, version2));
        when(mockVersioning.listTagsForCommit(version1)).thenReturn(List.of("v1.0.0", "user-supplied-name"));
        when(mockVersioning.listTagsForCommit(version2)).thenReturn(List.of("v2.0.0", "2nd-user-supplied-name"));

        when(mockVersionNameGenerator.isAutoGeneratedVersionName("v1.0.0")).thenReturn(true);
        when(mockVersionNameGenerator.isAutoGeneratedVersionName("v2.0.0")).thenReturn(true);
        when(mockVersionNameGenerator.isAutoGeneratedVersionName("user-supplied-name")).thenReturn(false);
        when(mockVersionNameGenerator.isAutoGeneratedVersionName("2nd-user-supplied-name")).thenReturn(false);

        var metadata = configurationRepositoryActions.getMetadataForAllConfigurationVersions(mockRepository);

        Assertions.assertThat(metadata)
                .hasSize(2)
                .containsExactly(
                        new ConfigurationVersion(version1, "v1.0.0", "user-supplied-name"),
                        new ConfigurationVersion(version2, "v2.0.0", "2nd-user-supplied-name")
                );
    }

    private ManagedRepositoryVersion mockConfigurationRead(String name, String versionId, boolean currentConfiguration, BaseAttributes... elements) {
        return mockConfigurationRead(name, versionId, null, null, currentConfiguration, elements);
    }

    private ManagedRepositoryVersion mockConfigurationRead(String name, String versionId, String versionName, String versionCustomName, boolean currentConfiguration, BaseAttributes... elements) {
        List<String> versionNames = Stream.of(versionName, versionCustomName)
                .filter(Objects::nonNull)
                .toList();

        var version = new ManagedRepositoryVersion(versionId, versionNames, getRepositoryObjects(elements));

        if (currentConfiguration) {
            when(mockRepository.getCurrentRepositoryVersion()).thenReturn(Optional.of(version));
        } else {
            when(mockRepository.getRepositoryVersion(versionId)).thenReturn(Optional.of(version));
        }

        var models = Arrays.stream(elements).filter(Model.class::isInstance).toArray(Model[]::new);
        var configVersion = new ConfigurationVersion(versionId, versionName, versionCustomName);

        doReturn(getConfiguration(name, configVersion, models))
                .when(mockTransformer).parseToConfiguration(
                        argThat(c -> contentsSize(c, elements.length)),
                        eq(name),
                        eq(configVersion));

        return version;
    }

    private void assertNameAndVersionMatchTestConstants(Configuration configuration) {
        Assertions.assertThat(configuration.getName()).isEqualTo(TEST_CONFIGURATION_NAME);
        Assertions.assertThat(configuration.getVersionHash()).isEqualTo(TEST_CONFIGURATION_HASH);
    }

    private boolean contentsEmpty(ConfigurationContents<?, ?, ?> content) {
        return content.getModels().isEmpty() && content.getNodes().isEmpty() && content.getRelations().isEmpty();
    }

    private boolean contentsSize(ConfigurationContents<?, ?, ?> content, int size) {
        return content.getModels().size() + content.getNodes().size() + content.getRelations().size() == size;
    }

    private Configuration getConfiguration(String name, ConfigurationVersion version, Model... models) {
        var configuration = new Configuration();
        configuration.setName(name);
        configuration.setVersion(version);
        configuration.setModels(new HashSet<>(Arrays.asList(models)));
        return configuration;
    }

    private Model getModel(Node... nodes) {
        var model = new Model();
        model.setNodes(new HashSet<>(Arrays.asList(nodes)));
        return model;
    }

    private Node getNode(Relation... relations) {
        var node = new Node();
        node.setRelations(new HashSet<>(Arrays.asList(relations)));
        return node;
    }

    private ConfigurationContents<DSLElement<Model>, DSLElement<Node>, DSLElement<Relation>> getContents(BaseAttributes... elements) {
        ConfigurationContents<DSLElement<Model>, DSLElement<Node>, DSLElement<Relation>> contents = new ConfigurationContents<>();
        for (var el : elements) {
            if (el instanceof Model) {
                contents.addModel(new DSLElement<>((Model) el, generateMockDSL()));
            } else if (el instanceof Node) {
                contents.addNode(new DSLElement<>((Node) el, generateMockDSL()));
            } else if (el instanceof Relation) {
                contents.addRelation(new DSLElement<>((Relation) el, generateMockDSL()));
            }
        }
        return contents;
    }

    private String generateMockDSL() {
        return UUID.randomUUID().toString();
    }

    private List<Path> getPaths(String... paths) {
        return Arrays.stream(paths).map(Path::of).toList();
    }

    private List<ManagedRepositoryObject> getRepositoryObjects(BaseAttributes... elements) {
        List<ManagedRepositoryObject> objects = new ArrayList<>();
        for (var el : elements) {
            if (el instanceof Model) {
                objects.add(mockModelObject((Model) el));
            } else if (el instanceof Node) {
                objects.add(mockNodeObject((Node) el));
            } else if (el instanceof Relation) {
                objects.add(mockRelationObject((Relation) el));
            }
        }
        return objects;
    }

    private ManagedRepositoryObject mockModelObject(Model model) {
        var id = model != null && model.getId() != null ? model.getId() : UUID.randomUUID().toString();

        var mockedModel = mock(ManagedRepositoryObject.class);
        lenient().when(mockedModel.rawFilePathMatches(MODELS_DIRECTORY.getBytes(encoding))).thenReturn(true);
        lenient().when(mockedModel.getFilePath()).thenReturn(MODELS_DIRECTORY + '/' + id + FILE_EXTENSION);
        lenient().when(mockedModel.getFileContent()).thenReturn(generateMockDSL());
        return mockedModel;
    }

    private ManagedRepositoryObject mockNodeObject(Node node) {
        var id = node != null && node.getId() != null ? node.getId() : UUID.randomUUID().toString();

        var mockedNode = mock(ManagedRepositoryObject.class);
        lenient().when(mockedNode.rawFilePathMatches(NODES_DIRECTORY.getBytes(encoding))).thenReturn(true);
        lenient().when(mockedNode.getFilePath()).thenReturn(NODES_DIRECTORY + '/' + id + FILE_EXTENSION);
        lenient().when(mockedNode.getFileContent()).thenReturn(generateMockDSL());
        return mockedNode;
    }

    private ManagedRepositoryObject mockRelationObject(Relation relation) {
        var id = relation != null && relation.getId() != null ? relation.getId() : UUID.randomUUID().toString();

        var mockedRelation = mock(ManagedRepositoryObject.class);
        lenient().when(mockedRelation.rawFilePathMatches(RELATIONS_DIRECTORY.getBytes(encoding))).thenReturn(true);
        lenient().when(mockedRelation.getFilePath()).thenReturn(RELATIONS_DIRECTORY + '/' + id + FILE_EXTENSION);
        lenient().when(mockedRelation.getFileContent()).thenReturn(generateMockDSL());
        return mockedRelation;
    }
}
