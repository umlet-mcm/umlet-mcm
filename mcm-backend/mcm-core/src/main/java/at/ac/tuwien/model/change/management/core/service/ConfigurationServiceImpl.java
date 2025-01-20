package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.versioning.BaseAttributesDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationProcessor;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationUtils;
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

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final VersionControlRepository versionControlRepository;
    private final GraphDBService graphDBService;

    @Override
    public Configuration createConfiguration(@NonNull Configuration configuration, boolean loadIntoGraphDB) {
        try {
            log.debug("Creating configuration '{}'.", configuration.getName());
            validateNewConfiguration(configuration);
            configuration.setName(ConfigurationUtils.sanitizeConfigurationName(configuration.getName()));
            configurationRepository.createConfiguration(configuration.getName());
            var savedConfiguration = configurationRepository.saveConfiguration(configuration);
            log.info("Created configuration '{}'.", configuration.getName());

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(savedConfiguration);
            }

            return savedConfiguration;
        } catch (RepositoryAlreadyExistsException e) {
            throw new ConfigurationAlreadyExistsException("Configuration with name '" + configuration.getName() + "' already exists.", e);
        } catch (RepositoryAccessException e) {
            // repository really should exist at this point
            throw new ConfigurationCreateException("Failed to create configuration with name '" + configuration.getName() + "'.", e);
        }
    }

    @Override
    public Configuration updateConfiguration(@NonNull Configuration configuration, boolean loadIntoGraphDB) {
        try {
            log.debug("Updating configuration '{}'.", configuration.getName());
            validateExistingConfiguration(configuration);
            configuration.setName(ConfigurationUtils.sanitizeConfigurationName(configuration.getName()));
            var savedConfiguration = configurationRepository.saveConfiguration(configuration);
            log.info("Updated configuration '{}'.", configuration.getName());

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(savedConfiguration);
            }

            return savedConfiguration;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not update configuration '" + configuration.getName() + "' because it was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationUpdateException("Failed to update configuration '" + configuration.getName() + "'.", e);
        }
    }

    @Override
    public void deleteConfiguration(@NonNull String name) {
        log.debug("Deleting configuration '{}'.", name);
        var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(name);
        try {
            configurationRepository.deleteConfiguration(sanitizedName);
            log.info("Deleted configuration '{}'.", sanitizedName);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationDeleteException("Failed to delete configuration '" + sanitizedName + "'.", e);
        }
    }

    @Override
    public Configuration getConfigurationByName(@NonNull String name, boolean loadIntoGraphDB) {
        log.debug("Finding current version of configuration '{}'.", name);
        var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(name);
        try {
            var foundConfiguration = configurationRepository.findCurrentVersionOfConfigurationByName(sanitizedName)
                    .orElseThrow(() -> new ConfigurationNotFoundException("Current version of configuration '" + sanitizedName + "' could not be found."));
            log.info("Found current version of configuration '{}'.", sanitizedName);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(foundConfiguration);
            }

            return foundConfiguration;
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to access current version of configuration '" + sanitizedName + "'.", e);
        }
    }

    @Override
    public Configuration getConfigurationVersion(@NonNull String name, @NonNull String version, boolean loadIntoGraphDB) {
        log.debug("Finding configuration '{}' with version '{}'.", name, version);
        var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(name);
        try {
            var foundConfiguration = configurationRepository.findSpecifiedVersionOfConfigurationByName(sanitizedName, version)
                    .orElseThrow(() -> new ConfigurationNotFoundException("Version '" + version + "' of configuration '" + sanitizedName + "' could not be found."));
            log.info("Found configuration version '{}' of configuration '{}'.", version, sanitizedName);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(foundConfiguration);
            }

            return foundConfiguration;
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to access version '" + version + "' of configuration '" + sanitizedName + "'.", e);
        }
    }

    @Override
    public List<Configuration> getAllConfigurations() {
        try {
            log.debug("Finding all configurations.");
            var configurations = configurationRepository.findAllConfigurations();
            log.info("Found {} configurations.", configurations.size());
            return configurations;
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to list all stored configurations.", e);
        }
    }

    @Override
    public List<String> listConfigurationVersions(@NonNull String configurationName) {
        try {
            log.debug("Listing all versions of configuration '{}'.", configurationName);
            var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(configurationName);
            var versions = versionControlRepository.listVersions(sanitizedName);
            log.info("Listed {} versions of configuration '{}'.", versions.size(), sanitizedName);
            return versions;
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to list all versions of configuration '" + configurationName + "'.", e);
        }
    }

    @Override
    public List<BaseAttributesDiff> compareConfigurationVersions(@NonNull String name, @NonNull String oldVersion, @NonNull String newVersion, boolean includeUnchanged) {
        try {
            log.debug("Comparing versions '{}' and '{}' of configuration '{}'.", oldVersion, newVersion, name);
            var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(name);
            var configurationContents = configurationRepository.compareConfigurationVersions(sanitizedName, oldVersion, newVersion, includeUnchanged);
            var diffs = new ArrayList<BaseAttributesDiff>();
            diffs.addAll(configurationContents.getModels());
            diffs.addAll(configurationContents.getNodes());
            diffs.addAll(configurationContents.getRelations());
            log.info("Created comparison of versions '{}' and '{}' of configuration '{}'.", oldVersion, newVersion, name);
            return diffs;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not compare versions because configuration '" + name + "' was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationComparisonException("Failed to compare versions '" + oldVersion +
                    "' and '" + newVersion + "' of configuration '" + name + "'.", e);
        }
    }

    @Override
    public Configuration checkoutConfigurationVersion(@NonNull String name, @NonNull String version, boolean loadIntoGraphDB) {
        try {
            log.debug("Checking out version '{}' of configuration '{}'.", version, name);
            var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(name);
            versionControlRepository.checkoutVersion(sanitizedName, version);
            var configuration = getConfigurationVersion(sanitizedName, version);
            log.info("Checked out version '{}' of configuration '{}'.", version, sanitizedName);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(configuration);
            }

            return configuration;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not checkout because configuration '" + name + "' was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationCheckoutException("Failed to checkout version '" + version + "' of configuration '" + name + "'.", e);
        }
    }

    @Override
    public Configuration resetConfigurationVersion(@NonNull String name, @Nullable String version, boolean loadIntoGraphDB) {
        try {
            log.debug("Resetting configuration '{}' to {}.", name,
                    version == null ? "current version" : "version '" + version + "'");
            var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(name);
            var resetVersion = version == null
                    ? versionControlRepository.getCurrentVersion(sanitizedName)
                    .orElseThrow(() -> new ConfigurationVersionDoesNotExistException("No versions exist for configuration '" + sanitizedName + "'."))
                    : version;
            versionControlRepository.resetToVersion(sanitizedName, resetVersion);
            var configuration = getConfigurationVersion(sanitizedName, resetVersion);
            log.info("Reset configuration '{}' to version '{}'.", sanitizedName, version);

            if (loadIntoGraphDB) {
                loadConfigurationIntoGraphDB(configuration);
            }

            return configuration;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Could not reset because configuration '" + name + "' was not found.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationResetException("Failed to reset configuration '" + name + "'.", e);
        }
    }

    private void validateNewConfiguration(Configuration configuration) {
        if (configuration.getVersion() != null) {
            throw new ConfigurationValidationException("New configuration cannot have a version.");
        }

        processModelElementIDs(configuration);
    }

    private void validateExistingConfiguration(Configuration configuration) {
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
}
