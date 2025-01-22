package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.ConfigurationVersion;
import at.ac.tuwien.model.change.management.core.model.versioning.BaseAttributesDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationProcessor;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.git.repository.VersionControlRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final VersionControlRepository versionControlRepository;
    private final GraphDBService graphDBService;
    private final NameValidationService nameValidationService;

    @Override
    public Configuration createConfiguration(@NonNull Configuration configuration, boolean loadIntoGraphDB) {
        try {
            log.debug("Creating configuration '{}'.", configuration.getName());
            validateNewConfiguration(configuration);
            configurationRepository.createConfiguration(configuration.getName());
            var savedConfiguration = configurationRepository.saveConfiguration(configuration);
            log.info("Created configuration '{}'.", configuration.getName());

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(savedConfiguration);
            }

            return decodeVersionName(savedConfiguration);
        } catch (Exception e) {
            // attempt rollback - delete repository if it was created
            try {
                configurationRepository.deleteConfiguration(configuration.getName());
            } catch (Exception ex) {
                log.error("Failed to rollback creation of configuration '{}'", configuration.getName(), ex);
            }

            if (e instanceof RepositoryAlreadyExistsException) {
                throw new ConfigurationAlreadyExistsException("Configuration with name '" + configuration.getName() + "' already exists.", e);
            }
            if (e instanceof RepositoryAccessException) {
                throw new ConfigurationCreateException("Failed to create configuration with name '" + configuration.getName() + "'.", e);
            }

            throw e;
        }
    }

    @Override
    public Configuration updateConfiguration(@NonNull Configuration configuration, boolean loadIntoGraphDB) {
        try {
            log.debug("Updating configuration '{}'.", configuration.getName());
            validateConfiguration(configuration);
            var savedConfiguration = configurationRepository.saveConfiguration(configuration);
            log.info("Updated configuration '{}'.", configuration.getName());

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(savedConfiguration);
            }

            return decodeVersionName(savedConfiguration);
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not update configuration '" + configuration.getName() + "' because it was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationUpdateException("Failed to update configuration '" + configuration.getName() + "'.", e);
        }
    }

    @Override
    public void deleteConfiguration(@NonNull String name) {
        log.debug("Deleting configuration '{}'.", name);
        validateConfigurationName(name);
        try {
            configurationRepository.deleteConfiguration(name);
            log.info("Deleted configuration '{}'.", name);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationDeleteException("Failed to delete configuration '" + name + "'.", e);
        }
    }

    @Override
    public Configuration renameConfiguration(@NonNull String currentName, @NonNull String newName) {
        log.debug("Renaming configuration '{}' to '{}'.", currentName, newName);

        validateConfigurationName(currentName);
        validateConfigurationName(newName);

        if (currentName.equals(newName)) {
            return configurationRepository.findCurrentVersionOfConfigurationByName(currentName)
                    .orElseThrow(() -> new ConfigurationNotFoundException("Configuration '" + currentName + "' not found."));
        }

        try {
            configurationRepository.renameConfiguration(currentName, newName);
            log.info("Renamed configuration '{}' to '{}'.", currentName, newName);

            var configuration = configurationRepository.findCurrentVersionOfConfigurationByName(newName)
                    .orElseThrow(() -> new ConfigurationNotFoundException("Failed to rename configuration '" + currentName + "' to '" + newName + "'.\n"
                            + "Renamed version of configuration '" + newName + "' could not be found."));
            return decodeVersionName(configuration);
        } catch (RepositoryAlreadyExistsException e) {
            throw new ConfigurationAlreadyExistsException("Configuration with name '" + newName + "' already exists.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationRenameException("Failed to rename configuration '" + currentName + "' to '" + newName + "'.", e);
        }
    }

    @Override
    public Configuration getConfigurationByName(@NonNull String name, boolean loadIntoGraphDB) {
        log.debug("Finding current version of configuration '{}'.", name);

        try {
            validateConfigurationName(name);
            var foundConfiguration = configurationRepository.findCurrentVersionOfConfigurationByName(name)
                    .orElseThrow(() -> new ConfigurationNotFoundException("Current version of configuration '" + name + "' could not be found."));
            log.info("Found current version of configuration '{}'.", name);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(foundConfiguration);
            }

            return decodeVersionName(foundConfiguration);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to access current version of configuration '" + name + "'.", e);
        }
    }

    @Override
    public Configuration getConfigurationVersion(@NonNull String name, @NonNull String version, boolean loadIntoGraphDB) {
        log.debug("Finding configuration '{}' with version '{}'.", name, version);
        try {
            validateConfigurationName(name);
            var encodedVersion = encodeVersionName(version);
            var foundConfiguration = configurationRepository.findSpecifiedVersionOfConfigurationByName(name, encodedVersion)
                    .orElseThrow(() -> new ConfigurationNotFoundException("Version '" + encodedVersion + "' of configuration '" + name + "' could not be found."));
            log.info("Found configuration version '{}' of configuration '{}'.", version, name);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(foundConfiguration);
            }

            return decodeVersionName(foundConfiguration);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to access version '" + version + "' of configuration '" + name + "'.", e);
        }
    }

    @Override
    public List<Configuration> getAllConfigurations() {
        try {
            log.debug("Finding all configurations.");
            var configurations = configurationRepository
                    .findAllConfigurations().stream().map(this::decodeVersionName)
                    .toList();
            log.info("Found {} configurations.", configurations.size());
            return configurations;
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to list all stored configurations.", e);
        }
    }

    @Override
    public List<ConfigurationVersion> listConfigurationVersions(@NonNull String configurationName) {
        try {
            log.debug("Listing all versions of configuration '{}'.", configurationName);
            validateConfigurationName(configurationName);
            var versions = configurationRepository.listConfigurationVersions(configurationName).stream()
                    .map(this::decodeVersionName)
                    .toList();
            log.info("Listed {} versions of configuration '{}'.", versions.size(), configurationName);
            return versions;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not list versions because configuration '" + configurationName + "' was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to list all versions of configuration '" + configurationName + "'.", e);
        }
    }

    @Override
    public List<BaseAttributesDiff> compareConfigurationVersions(@NonNull String name, @NonNull String oldVersion, @NonNull String newVersion, boolean includeUnchanged) {
        log.debug("Comparing versions '{}' and '{}' of configuration '{}'.", oldVersion, newVersion, name);
        validateConfigurationName(name);
        var oldVersionEncoded = encodeVersionName(oldVersion);
        var newVersionEncoded = encodeVersionName(newVersion);

        try {
            var configurationContents = configurationRepository.compareConfigurationVersions(name, oldVersionEncoded, newVersionEncoded, includeUnchanged);
            var diffs = new ArrayList<BaseAttributesDiff>();
            diffs.addAll(configurationContents.getModels());
            diffs.addAll(configurationContents.getNodes());
            diffs.addAll(configurationContents.getRelations());
            log.info("Created comparison of versions '{}' and '{}' of configuration '{}'.", oldVersion, newVersion, name);
            return diffs;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not compare versions because configuration '" + name + "' was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationComparisonException("Failed to compare versions '" + oldVersionEncoded +
                    "' and '" + newVersionEncoded + "' of configuration '" + name + "'.", e);
        }
    }

    @Override
    public Configuration checkoutConfigurationVersion(@NonNull String name, @NonNull String version, boolean loadIntoGraphDB) {
        log.debug("Checking out version '{}' of configuration '{}'.", version, name);
        validateConfigurationName(name);
        var versionEncoded = encodeVersionName(version);

        try {
            versionControlRepository.checkoutVersion(name, versionEncoded);
            var configuration = configurationRepository.findSpecifiedVersionOfConfigurationByName(name, versionEncoded)
                    .orElseThrow(() -> new ConfigurationNotFoundException(
                            "Could not find version '" + version + "' of configuration '" + name + "' after checkout."
                    ));

            log.info("Checked out version '{}' of configuration '{}'.", version, name);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(configuration);
            }

            return decodeVersionName(configuration);
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not checkout because configuration '" + name + "' was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationCheckoutException("Failed to checkout version '" + versionEncoded + "' of configuration '" + name + "'.", e);
        }
    }

    @Override
    public Configuration resetConfigurationVersion(@NonNull String name, @Nullable String version, boolean loadIntoGraphDB) {
        try {
            log.debug("Resetting configuration '{}' to {}.", name,
                    version == null ? "current version" : "version '" + version + "'");
            validateConfigurationName(name);

            var resetVersion = version == null
                    ? versionControlRepository.getCurrentVersion(name)
                    .orElseThrow(() -> new ConfigurationVersionDoesNotExistException("No versions exist for configuration '" + name + "'."))
                    : encodeVersionName(version);
            versionControlRepository.resetToVersion(name, resetVersion);
            var configuration = configurationRepository.findSpecifiedVersionOfConfigurationByName(name, resetVersion)
                    .orElseThrow(() -> new ConfigurationNotFoundException(
                            "Could not find version '" + resetVersion + "' of configuration '" + name + "' after reset."
                    ));
            log.info("Reset configuration '{}' to version '{}'.", name, version);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(configuration);
            }

            return decodeVersionName(configuration);
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not reset because configuration '" + name + "' was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationResetException("Failed to reset configuration '" + name + "'.", e);
        }
    }

    private void validateNewConfiguration(Configuration configuration) {
        if (configuration.getVersionHash() != null) {
            throw new ConfigurationValidationException("New configuration cannot have a version.");
        }
        validateConfiguration(configuration);
    }

    private void validateConfiguration(Configuration configuration) {
        validateConfigurationName(configuration.getName());
        encodeVersionName(configuration);
        processModelElementIDs(configuration);
    }

    private void processModelElementIDs(Configuration configuration) {
        var elementIDs = new ArrayList<String>();
        var configurationProcessor = new ConfigurationProcessor(configuration);
        configurationProcessor.processModels(model -> {
            if (model.getId() != null) elementIDs.add(model.getId());
        });

        configurationProcessor.processNodes((node, model) -> {
            if (node.getId() != null) elementIDs.add(node.getId());
            if (node.getMcmModelId() == null) node.setMcmModelId(model.getId());
            else if (!node.getMcmModelId().equals(model.getId())) {
                throw new ConfigurationValidationException("Node '" + node.getId() + "' does not belong to the model it is assigned to.");
            }
        });

        configurationProcessor.processRelations((relation, node) -> {
            if (relation.getId() != null) elementIDs.add(relation.getId());
            if (relation.getMcmModelId() == null) relation.setMcmModelId(node.getMcmModelId());
            else if (!relation.getMcmModelId().equals(node.getMcmModelId())) {
                throw new ConfigurationValidationException("Relation '" + relation.getId() + "' does not belong to the model it is assigned to.");
            }
        });

        checkForDuplicateIDs(elementIDs);
    }

    private void checkForDuplicateIDs(List<String> ids) {
        var numberOfIDsIncludingDuplicates = ids.size();
        var numberOfIDsWithoutDuplicates = new HashSet<>(ids).size();
        if (numberOfIDsIncludingDuplicates != numberOfIDsWithoutDuplicates) {
            throw new ConfigurationValidationException("Configuration contains " + (numberOfIDsIncludingDuplicates - numberOfIDsWithoutDuplicates) + " duplicate element IDs.");
        }
    }

    private void loadConfigurationIntoGraphDB(Configuration configuration) {
        // Load the configuration into the graph database
        graphDBService.clearDatabase();
        graphDBService.loadConfiguration(configuration);
        log.info("Loaded configuration '{}' into graph database.", configuration.getName());
    }

    private void validateConfigurationName(String name) {
        try {
            if (name == null || name.isBlank()) {
                throw new ConfigurationValidationException("Configuration name cannot be null or empty.");
            }
            nameValidationService.validateRepositoryName(name);
        } catch (InvalidNameException e) {
            throw new ConfigurationValidationException("Configuration name '" + name + "' is invalid.", e);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private Configuration encodeVersionName(Configuration configuration) {
        var originalVersion = configuration.getVersion();
        if (originalVersion != null && originalVersion.customName() != null) {
            var encodedVersion = originalVersion.withCustomName(encodeVersionName(originalVersion.customName()));
            configuration.setVersion(encodedVersion);
        }
        return configuration;
    }

    private String encodeVersionName(String version) {
        return version == null
                ? null
                : nameValidationService.encodeVersionName(version, true);
    }

    private Configuration decodeVersionName(Configuration configuration) {
        configuration.setVersion(decodeVersionName(configuration.getVersion()));
        return configuration;
    }

    private ConfigurationVersion decodeVersionName(ConfigurationVersion version) {
        return version == null || version.customName() == null
                ? version
                : version.withCustomName(decodeVersionName(version.customName()));
    }

    private String decodeVersionName(String version) {
        return version == null
                ? null
                : nameValidationService.decodeVersionName(version);
    }
}
