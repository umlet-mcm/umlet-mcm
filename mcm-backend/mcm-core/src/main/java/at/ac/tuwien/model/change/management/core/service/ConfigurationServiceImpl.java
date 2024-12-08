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

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final VersionControlRepository versionControlRepository;
    private final GraphDBService graphDBService;

    @Override
    public Configuration createConfiguration(@NonNull Configuration configuration) {
        try {
            log.debug("Creating configuration '{}'.", configuration.getName());
            validateNewConfiguration(configuration);
            configuration.setName(ConfigurationUtils.sanitizeConfigurationName(configuration.getName()));
            configurationRepository.createConfiguration(configuration.getName());
            var savedConfiguration = configurationRepository.saveConfiguration(configuration);
            log.info("Created configuration '{}'.", configuration.getName());

            // Load the configuration into the graph database
            graphDBService.clearDatabase();
            graphDBService.loadConfiguration(configuration);

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

            // Load the configuration into the graph database
            graphDBService.clearDatabase();
            graphDBService.loadConfiguration(configuration);

            return savedConfiguration;
        } catch (RepositoryDoesNotExistException e) {
            throw new ConfigurationDoesNotExistException("Configuration '" + configuration.getName() + "' does not exist.", e);
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
        if (configuration.getVersion() != null) {
            throw new ConfigurationValidationException("New configuration cannot have a version.");
        }

        var elementIDs = new ArrayList<String>();
        for (var model : tryAccessCollection(configuration.getModels())) {
            if (model.getId() != null) elementIDs.add(model.getId());
            for (var node : tryAccessCollection(model.getNodes())) {
                if (node.getId() != null) elementIDs.add(node.getId());
                if (node.getMcmModelId() != null && ! node.getMcmModelId().equals(model.getId())) {
                    throw new ConfigurationValidationException("Node does not belong to the model it is assigned to.");
                }
                for (var relation : tryAccessCollection(node.getRelations())) {
                    if (relation.getId() != null) elementIDs.add(relation.getId());
                    if (relation.getMcmModelId() != null && ! relation.getMcmModelId().equals(model.getId())) {
                        throw new ConfigurationValidationException("Relation does not belong to the model it is assigned to.");
                    }
                }
            }
        }
        checkForDuplicateElementIDs(elementIDs);
    }

    private void validateExistingConfiguration(Configuration configuration) {
        var elementIDs = new ArrayList<String>();
        for (var model : tryAccessCollection(configuration.getModels())) {
            if (model.getId() != null) elementIDs.add(model.getId());
            for (var node : tryAccessCollection(model.getNodes())) {
                if (node.getId() != null) elementIDs.add(node.getId());
                if (node.getMcmModelId() == null) node.setMcmModelId(model.getId());
                else if (! node.getMcmModelId().equals(model.getId())) {
                    throw new ConfigurationValidationException("Node does not belong to the model it is assigned to.");
                }
                for (var relation : tryAccessCollection(node.getRelations())) {
                    if (relation.getId() != null) elementIDs.add(relation.getId());
                    if (relation.getMcmModelId() == null) relation.setMcmModelId(model.getId());
                    else if (! relation.getMcmModelId().equals(model.getId())) {
                        throw new ConfigurationValidationException("Relation does not belong to the model it is assigned to.");
                    }
                }
            }
        }
        checkForDuplicateElementIDs(elementIDs);
    }

    private void checkForDuplicateElementIDs(List<String> elementIDs) {
        if (elementIDs.size() != new HashSet<>(elementIDs).size()) {
            throw new ConfigurationValidationException("Configuration contains duplicate element IDs");
        }
    }

    private <T> Collection<T> tryAccessCollection(Collection<T> collection) {
        return Optional.ofNullable(collection).orElseGet(Collections::emptyList);
    }
}
