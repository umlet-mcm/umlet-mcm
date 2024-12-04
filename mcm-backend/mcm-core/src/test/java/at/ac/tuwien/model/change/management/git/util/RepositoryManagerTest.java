package at.ac.tuwien.model.change.management.git.util;

//public class RepositoryManagerTest {
//
//    @TempDir
//    private Path tempDir;
//    private RepositoryManager repositoryManager;
//
//    @BeforeEach
//    public void setup() {
//        var gitProperties = new GitProperties();
//        gitProperties.setRepositoryPath(tempDir);
//        var mockDSLTransformer = new MockDSLTransformer();
//        repositoryManager = new RepositoryManager(gitProperties, mockDSLTransformer);
//    }
//
//    @Test
//    public void testWithRepository_createRepository_shouldCreateRepository() {
//        var repositoryName = "test";
//        var repository = repositoryManager.withRepository(repositoryName, false, repo -> {
//            try {
//                repo.create();
//                return repo;
//            } catch (IOException e) {
//                throw new ConfigurationCreateException("Failed to create configuration", e);
//            }
//        });
//        Assertions.assertThat(tempDir.resolve(repositoryName).resolve(".git")).exists();
//        Assertions.assertThat(RepositoryUtils.repositoryExists(repository)).isTrue();
//    }
//
//    @Test
//    public void testWithRepository_whenMustExistTrue__shouldThrowException() {
//        var repositoryName = "test";
//        Assertions.assertThatThrownBy(() -> repositoryManager.withRepository(repositoryName, true, repository -> repository))
//                .isInstanceOf(ConfigurationReadException.class);
//    }
//
//    @Test
//    public void testWithRepository_whenMustExistFalse_shouldNotThrowException() {
//        var repositoryName = "test";
//        Assertions.assertThatCode(() -> repositoryManager.withRepository(repositoryName, false, repository -> repository))
//                .doesNotThrowAnyException();
//    }
//
//    @Test
//    public void testWithRepository_whenMustExistTrueAndRepositoryExists_shouldNotThrowException() {
//        var repositoryName = "test";
//        repositoryManager.withRepository(repositoryName, false, this::createRepository);
//        Assertions.assertThatCode(() -> repositoryManager.withRepository(repositoryName, true, repository -> repository))
//                .doesNotThrowAnyException();
//    }
//
//    @Test
//    public void testWithGit_whenRepositoryDoesNotExist_shouldThrowException() {
//        var repositoryName = "test";
//        Assertions.assertThatThrownBy(() -> repositoryManager.withGit(repositoryName, git -> git))
//                .isInstanceOf(ConfigurationReadException.class);
//    }
//
//    @Test
//    public void testWithGit_whenRepositoryExists_shouldNotThrowException() {
//        var repositoryName = "test";
//        repositoryManager.withRepository(repositoryName, false, this::createRepository);
//        Assertions.assertThatCode(() -> repositoryManager.withGit(repositoryName, git -> git))
//                .doesNotThrowAnyException();
//    }
//
//    @Test
//    public void testGitRepositoriesPath_shouldReturnCorrectPath() {
//        Assertions.assertThat(repositoryManager.gitRepositoriesPath()).isEqualTo(tempDir);
//    }
//
//    @Test
//    public void testWriteConfigurationToRepository_shouldWriteCorrectNumberOfFilesToRepository() {
//        var repositoryName = "test";
//        var numNodes = 3;
//        var numRelationsPerNode = 3;
//
//        var repository = repositoryManager.withRepository(repositoryName, false, this::createRepository);
//
//        var configuration = new Configuration();
//        configuration.setName(repositoryName);
//
//        var model = DomainModelGen.generateFullyRandomizedModel(numNodes, numRelationsPerNode);
//        configuration.setModels(new HashSet<>(List.of(model)));
//
//        repositoryManager.writeConfigurationToRepository(configuration, repository);
//
//        var repositoryWorkPath = repository.getWorkTree().toPath();
//        var modelFiles = listFilesInDirectory(repositoryWorkPath.resolve("models"));
//        var nodeFiles = listFilesInDirectory(repositoryWorkPath.resolve("nodes"));
//        var relationFiles = listFilesInDirectory(repositoryWorkPath.resolve("relations"));
//
//        Assertions.assertThat(modelFiles).hasSize(1);
//        Assertions.assertThat(nodeFiles).hasSize(numNodes);
//        Assertions.assertThat(relationFiles).hasSize(numNodes * numRelationsPerNode);
//    }
//
//    @Test
//    public void testWriteRead_emptyConfiguration_shouldWriteAndReadSameConfiguration() {
//        var repositoryName = "test";
//        var repository = repositoryManager.withRepository(repositoryName, false, this::createRepository);
//        var configuration = new Configuration();
//        configuration.setName(repositoryName);
//        repositoryManager.writeConfigurationToRepository(configuration, repository);
//        commitRepository(repository);
//
//        var readConfiguration = repositoryManager.readConfigurationFromRepository(repositoryName);
//
//        Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
//        Assertions.assertThat(readConfiguration)
//                .usingRecursiveComparison()
//                .ignoringFields("version")
//                .isEqualTo(configuration);
//    }
//
//    @Test
//    public void testWriteRead_configurationWithModels_shouldWriteAndReadSameConfiguration() {
//        var repositoryName = "test";
//        var repository = repositoryManager.withRepository(repositoryName, false, this::createRepository);
//        var configuration = new Configuration();
//        configuration.setName(repositoryName);
//        var model = DomainModelGen.generateFullyRandomizedModel(0, 0);
//        configuration.setModels(new HashSet<>(List.of(model)));
//        repositoryManager.writeConfigurationToRepository(configuration, repository);
//
//        commitRepository(repository);
//        var readConfiguration = repositoryManager.readConfigurationFromRepository(repositoryName);
//
//        Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
//        Assertions.assertThat(readConfiguration)
//                .usingRecursiveComparison()
//                .ignoringFields("version")
//                .isEqualTo(configuration);
//    }
//
//    @Test
//    public void testWriteRead_configurationWithNodes_shouldWriteAndReadSameConfiguration() {
//        var repositoryName = "test";
//        var repository = repositoryManager.withRepository(repositoryName, false, this::createRepository);
//        var configuration = new Configuration();
//        configuration.setName(repositoryName);
//        var model = DomainModelGen.generateFullyRandomizedModel(3, 0);
//        configuration.setModels(new HashSet<>(List.of(model)));
//        repositoryManager.writeConfigurationToRepository(configuration, repository);
//        commitRepository(repository);
//
//        var readConfiguration = repositoryManager.readConfigurationFromRepository(repositoryName);
//
//        Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
//        Assertions.assertThat(readConfiguration)
//                .usingRecursiveComparison()
//                .ignoringFields("version")
//                .isEqualTo(configuration);
//    }
//
//    @Test
//    public void testWriteRead_configurationWithRelations_shouldWriteAndReadSameConfiguration() {
//        var repositoryName = "test";
//        var repository = repositoryManager.withRepository(repositoryName, false, this::createRepository);
//        var configuration = new Configuration();
//        configuration.setName(repositoryName);
//        var model = DomainModelGen.generateFullyRandomizedModel(3, 3);
//        configuration.setModels(new HashSet<>(List.of(model)));
//        repositoryManager.writeConfigurationToRepository(configuration, repository);
//        commitRepository(repository);
//
//        var readConfiguration = repositoryManager.readConfigurationFromRepository(repositoryName);
//
//        Assertions.assertThat(readConfiguration.getName()).isEqualTo(configuration.getName());
//        Assertions.assertThat(readConfiguration.getModels()).isEqualTo(configuration.getModels());
//        Assertions.assertThat(readConfiguration.getVersion()).isNotNull();
//    }
//
//    private Repository createRepository(Repository repository) {
//        try {
//            repository.create();
//            return repository;
//        } catch (IOException e) {
//            throw new ConfigurationCreateException("Failed to create configuration", e);
//        }
//    }
//
//    private List<Path> listFilesInDirectory(Path path) {
//        try (var fileStream = Files.list(path)) {
//            return fileStream.toList();
//        } catch (IOException e) {
//            throw new ConfigurationReadException("Failed to list files in directory", e);
//        }
//    }
//
//    private void commitRepository(Repository repository) {
//        try {
//            var git = Git.wrap(repository);
//            git.add().addFilepattern(".").call();
//            git.commit().setMessage("Test commit").call();
//        } catch (GitAPIException e) {
//            throw new ConfigurationWriteException("Failed to update configuration", e);
//        }
//    }
//}
