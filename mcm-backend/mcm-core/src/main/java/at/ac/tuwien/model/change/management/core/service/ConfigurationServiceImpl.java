package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationUtils;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.git.repository.VersionControlRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final VersionControlRepository versionControlRepository;

    @Override
    public Configuration createConfiguration(@NonNull Configuration configuration) {
        try {
            log.debug("Creating configuration '{}'.", configuration.getName());
            validateNewConfiguration(configuration);
            configuration.setName(ConfigurationUtils.sanitizeConfigurationName(configuration.getName()));
            configurationRepository.createConfiguration(configuration.getName());
            var savedConfiguration = configurationRepository.saveConfiguration(configuration);
            log.info("Created configuration '{}'.", configuration.getName());
            return savedConfiguration;
        } catch (RepositoryAlreadyExistsException e) {
            throw new ConfigurationAlreadyExistsException("Configuration with name '" + configuration.getName() + "' already exists.", e);
        } catch (RepositoryAccessException e) {
            // repository really should exist at this point
            throw new ConfigurationCreateException("Failed to create configuration with name '" + configuration.getName() + "'.", e);
        }
    }

    @Override
    public Configuration updateConfiguration(@NonNull Configuration configuration) {
        try {
            log.debug("Updating configuration '{}'.", configuration.getName());
            validateExistingConfiguration(configuration);
            configuration.setName(ConfigurationUtils.sanitizeConfigurationName(configuration.getName()));
            var currentVersion = versionControlRepository.getCurrentVersion(configuration.getName());
            var updateVersion = Optional.ofNullable(configuration.getVersion()).orElseThrow(() ->
                    new ConfigurationValidationException("Configuration '" + configuration.getName() +
                            "' cannot be updated, because its version is not specified."));
            if (! updateVersion.equals(currentVersion)) {
                throw new ConfigurationUpdateException("Version of configuration '" + configuration.getName() +
                        "' does not match the current version. Merging of divergent versions is not yet supported.");
            }
            var savedConfiguration = configurationRepository.saveConfiguration(configuration);
            log.info("Updated configuration '{}'.", configuration.getName());
            return savedConfiguration;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Configuration '" + configuration.getName() + "' does not exist.", e);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationUpdateException("Failed to update configuration '" + configuration.getName() + "'.", e);
        }
    }

    @Override
    public void deleteConfiguration(@NonNull String configurationName) {
        log.debug("Deleting configuration '{}'.", configurationName);
        var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(configurationName);
        try {
            configurationRepository.deleteConfiguration(sanitizedName);
            log.info("Deleted configuration '{}'.", sanitizedName);
        } catch (RepositoryAccessException e) {
            throw new ConfigurationDeleteException("Failed to delete configuration '" + sanitizedName + "'.", e);
        }
    }

    @Override
    public Configuration getConfigurationByName(@NonNull String name) {
        log.debug("Finding configuration '{}'.", name);
        var sanitizedName = ConfigurationUtils.sanitizeConfigurationName(name);
        try {
            var foundConfiguration = configurationRepository.findConfigurationByName(sanitizedName)
                    .orElseThrow(() -> new ConfigurationNotFoundException("Configuration '" + sanitizedName + "' does not exist."));
            log.info("Found configuration '{}'.", sanitizedName);
            return foundConfiguration;
        } catch (RepositoryAccessException e) {
            throw new ConfigurationGetException("Failed to access configuration '" + sanitizedName + "'.", e);
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

    private void validateNewConfiguration(Configuration configuration) {
        for (var model : tryAccessCollection(configuration.getModels())) {
            if (model.getId() != null) {
                throw new ConfigurationValidationException("Model cannot have an id when creating a new configuration.");
            }
            for (var node : tryAccessCollection(model.getNodes())) {
                if (node.getId() != null) {
                    throw new ConfigurationValidationException("Node cannot have an id when creating a new configuration.");
                }
                for (var relation : tryAccessCollection(node.getRelations())) {
                    if (relation.getId() != null) {
                        throw new ConfigurationValidationException("Relation cannot have an id when creating a new configuration.");
                    }
                }
            }
        }
    }

    private void validateExistingConfiguration(Configuration configuration) {
        for (var model : tryAccessCollection(configuration.getModels())) {
            for (var node : tryAccessCollection(model.getNodes())) {
                if (node.getMcmModelId() == null) node.setMcmModelId(model.getId());
                else if (! node.getMcmModelId().equals(model.getId())) {
                    throw new ConfigurationValidationException("Node does not belong to the model it is assigned to.");
                }
            }
        }
    }

    private <T> Collection<T> tryAccessCollection(Collection<T> collection) {
        return Optional.ofNullable(collection).orElseGet(Collections::emptyList);
    }
}
