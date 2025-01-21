package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.mapper.uxf.ConfigurationUxfMapper;
import at.ac.tuwien.model.change.management.core.mapper.uxf.ModelUxfMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.ConfigurationVersion;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.attributes.AttributeKeys;
import at.ac.tuwien.model.change.management.core.model.intermediary.ConfigurationUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.utils.ConfigurationUtils;
import at.ac.tuwien.model.change.management.core.model.utils.RelationUtils;
import at.ac.tuwien.model.change.management.core.transformer.XMLTransformer;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UxfServiceImpl implements UxfService {

    private final ConfigurationService configurationService;

    private final XMLTransformer xmlTransformer;

    @Override
    public Configuration createConfigurationFromUxf(InputStreamSource file, String name, String version) throws UxfException {
        Model parsedModel = parseUxf(file);

        Configuration newConf = isFullConfiguration(parsedModel)
                ? ConfigurationUtils.processImportedConfiguration(parsedModel)
                : newConfiguration(parsedModel, name);

        setConfigurationName(newConf, name);
        setConfigurationVersion(newConf, version);

        return configurationService.createConfiguration(newConf);
    }

    @Override
    public Configuration updateConfigurationFromUxf(InputStreamSource file, String configurationName, String version) throws UxfException {
        if (configurationName == null) {
            throw new ConfigurationValidationException("Could not update configuration from uxf because configuration name was not provided");
        }

        Model parsedModel = parseUxf(file);
        Configuration configuration;

        // Case 1: The UXF contains a configuration ID/name field
        // hence we assume that it represents a full configuration, and we are trying to update that configuration
        // Note that for naming the configuration (and hence matching it to the
        if (isFullConfiguration(parsedModel)) {
            configuration = ConfigurationUtils.processImportedConfiguration(parsedModel);
        }

        // Case 2: The UXF contains no configuration ID/name field
        // But it does contain a mcm model ID
        // we try to match it to the model from the configuration whose name we were provided
        else if (parsedModel.getId() != null) {
            configuration = configurationService.getConfigurationByName(configurationName);
            var models = configuration.getModels();
            boolean found = models.removeIf(existingModel -> Objects.equals(existingModel.getId(), parsedModel.getId()));
            if (!found) {
                throw new ModelNotFoundException("Model with id '" + parsedModel.getId() + "' not found in configuration '" + configurationName + "'");
            }
            models.add(parsedModel);
        }

        // Case 3: The UXF contains no configuration ID/name field nor a model ID
        // In this case it is considered just the sole and lonely model in the new configuration
        // and we replace the previous configuration with it
        // TODO: is this desired behavior? We could also add the model to the configuration, but that's already implemented elsewhere
        else {
            configuration = newConfiguration(parsedModel, configurationName);
        }

        setConfigurationName(configuration, configurationName);
        setConfigurationVersion(configuration, version);

        return configurationService.updateConfiguration(configuration);
    }

    @Override
    public Configuration addUxfToConfiguration(InputStreamSource file, String configurationUUID, String modelName) throws UxfException, ConfigurationException {
        Configuration target;
        try {
            target = configurationService.getConfigurationByName(configurationUUID);
        } catch (ConfigurationGetException e) {
            throw new ConfigurationException("Could not get configuration", e);
        }

        Model parsedModel = parseUxf(file);

        String modelTitle = modelName;
        if (modelTitle == null) {
            modelTitle = UUID.randomUUID().toString();
        }
        parsedModel.setTitle(modelTitle);
        target.getModels().add(parsedModel);
        return configurationService.updateConfiguration(target);
    }

    @Override
    public String exportModel(String configurationName, String modelUuid) throws ModelNotFoundException, UxfException {
        Model target = null;
        Configuration targetConf = configurationService.getConfigurationByName(configurationName);

        for (Model model : targetConf.getModels()) {
            if (Objects.equals(model.getId(), modelUuid)) {
                target = model;
                break;
            }
        }

        if (target == null) {
            throw new ModelNotFoundException("Model with id '" + modelUuid + "' not found in configuration '" + configurationName + "'");
        }

        ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);
        ModelUxf modelUxf = modelUxfMapper.fromModel(target);

        try {
            return xmlTransformer.marshalUxf(modelUxf);
        } catch (
                JAXBException e) {
            throw new UxfException("Could not marshal model to XML", e);
        }
    }

    @Override
    public String exportModel(Model model) throws UxfException {
        ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);
        ModelUxf modelUxf = modelUxfMapper.fromModel(model);

        try {
            return xmlTransformer.marshalUxf(modelUxf);
        } catch (JAXBException e) {
            throw new UxfException("Could not marshal model to XML", e);
        }
    }

    @Override
    public String exportConfiguration(String configurationUuid) throws ConfigurationException, UxfException {
        Configuration target;
        try {
            target = configurationService.getConfigurationByName(configurationUuid);
        } catch (ConfigurationGetException e) {
            throw new ConfigurationException("Could not get configuration", e);
        }

        ConfigurationUxf configurationUxf = ConfigurationUxfMapper.toConfigurationUxf(target);

        try {
            return xmlTransformer.marshalUxf(configurationUxf);
        } catch (JAXBException e) {
            throw new UxfException("Could not marshal configuration to XML", e);
        }
    }

    private Model parseUxf(InputStreamSource file) throws UxfException {
        InputStream input;

        try {
            input = file.getInputStream();
        } catch (IOException e) {
            throw new UxfException("Error opening file '" + file + "'", e);
        }

        try {
            ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);

            // the uxf is first unmarshalled into the intermediary classes
            ModelUxf modelUxf = (ModelUxf) xmlTransformer.unmarshal(input);
            // map the intermediary to the actual model
            Model mappedModel = modelUxfMapper.toModel(modelUxf);

            return RelationUtils.processRelations(mappedModel);
        } catch (JAXBException e) {
            throw new UxfException("Error parsing input", e);
        }
    }

    private Configuration newConfiguration(Model model, String name) {
        Configuration configuration = new Configuration();
        String confName = name;
        if (confName == null) {
            confName = UUID.randomUUID().toString();
        }
        configuration.setName(confName);
        configuration.getModels().add(model);
        model.setTitle(confName);
        return configuration;
    }

    private void setConfigurationName(Configuration configuration, @Nullable String configurationName) {
        // in order of preference choose
        // 1. the configuration name provided as request param by the user
        // 2. the configuration name parsed from the UXF
        // 3. a random, newly generated UUID
        if (configurationName != null) {
            configuration.setName(configurationName);
        } else if (configuration.getName() == null) {
            configuration.setName(UUID.randomUUID().toString());
        }
    }

    private void setConfigurationVersion(Configuration configuration, String version) {
        if (version == null) return;
        configuration.setVersion(new ConfigurationVersion(null, null, version));
    }

    @Override
    public Model updateModelFromUxf(InputStreamSource file, String newModelName) throws UxfException {
        Model parsedModel = parseUxf(file);
        parsedModel.setTitle(newModelName);
        if (parsedModel.getId() == null) {
            throw new UxfException("Model id is missing");
        }
        var configs = configurationService.getAllConfigurations();
        for (var conf : configs) {
            var models = conf.getModels();
            boolean found = models.removeIf(existingModel -> Objects.equals(existingModel.getId(), parsedModel.getId()));
            if (found) {
                models.add(parsedModel);
                configurationService.updateConfiguration(conf);
                return parsedModel;
            }

        }
        throw new ModelNotFoundException("Model with id '" + parsedModel.getId() + "' not found");
    }

    private boolean isFullConfiguration(Model parsedModel) {
        return parsedModel.getMcmAttributes() != null && parsedModel.getMcmAttributes().get(AttributeKeys.CONFIGURATION_ID) != null;
    }
}
