package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.ConfigurationGetException;
import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.mapper.uxf.ConfigurationUxfMapper;
import at.ac.tuwien.model.change.management.core.mapper.uxf.ModelUxfMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.intermediary.ConfigurationUxf;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.utils.RelationUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UxfServiceImpl implements UxfService {

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public Configuration createConfigurationFromUxf(InputStreamSource file) throws UxfException {
        Model parsedModel = parseUxf(file);
        Configuration newConf = newConfiguration(parsedModel);
        configurationService.createConfiguration(newConf);
        return newConf;
    }

    @Override
    public Configuration addUxfToConfiguration(InputStreamSource file, String configurationUUID) throws UxfException, ConfigurationException {
        Configuration target;
        try {
            target = configurationService.getConfigurationByName(configurationUUID);
        } catch (ConfigurationGetException e) {
            throw new ConfigurationException("Could not get configuration", e.getCause());
        }

        Model parsedModel = parseUxf(file);
        target.getModels().add(parsedModel);
        configurationService.updateConfiguration(target);
        return target;
    }

    @Override
    public String exportModel(String modelUuid) throws ModelNotFoundException, UxfException {
        List<Configuration> configurations = configurationService.getAllConfigurations();
        Model target = null;
        for (Configuration conf : configurations) {
            for (Model model : conf.getModels()) {
                if (model.getId().equals(modelUuid)) {
                    target = model;
                    break;
                }
            }
        }

        if (target == null) {
            throw new ModelNotFoundException("Model with id '" + modelUuid + "' not found in any configuration");
        }

        JAXBContext context;
        Marshaller marshaller;
        try {
            context = JAXBContext.newInstance(ModelUxf.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // do not generate enclosing <xml> tag
        } catch (JAXBException e) {
            throw new UxfException("Could not create marshaller", e.getCause());
        }

        ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);
        ModelUxf modelUxf = modelUxfMapper.fromModel(target);

        StringWriter writer = new StringWriter();
        try {
            marshaller.marshal(modelUxf, writer);
        } catch (JAXBException e) {
            throw new UxfException("Could not marshal model to XML", e.getCause());
        }

        return writer.toString();
    }

    @Override
    public String exportConfiguration(String configurationUuid) throws ConfigurationException, UxfException {
        Configuration target;
        try {
            target = configurationService.getConfigurationByName(configurationUuid);
        } catch (ConfigurationGetException e) {
            throw new ConfigurationException("Could not get configuration", e.getCause());
        }

        JAXBContext context;
        Marshaller marshaller;
        try {
            context = JAXBContext.newInstance(ConfigurationUxf.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // do not generate enclosing <xml> tag
        } catch (JAXBException e) {
            throw new UxfException("Could not create marshaller", e.getCause());
        }

        ConfigurationUxf configurationUxf = ConfigurationUxfMapper.toConfigurationUxf(target);

        StringWriter writer = new StringWriter();
        try {
            marshaller.marshal(configurationUxf, writer);
        } catch (JAXBException e) {
            throw new UxfException("Could not marshal configuration to XML", e.getCause());
        }

        return writer.toString();
    }

    private Model parseUxf(InputStreamSource file) throws UxfException {
        InputStream input;
        JAXBContext context;
        Unmarshaller unmarshaller;
        try {
            context = JAXBContext.newInstance(ModelUxf.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new UxfException("Could not create unmarshaller", e.getCause());
        }

        try {
            input = file.getInputStream();
        } catch (IOException e) {
            throw new UxfException("Error opening file '" + file + "'", e.getCause());
        }

        try {
            ModelUxfMapper modelUxfMapper = Mappers.getMapper(ModelUxfMapper.class);

            // the uxf is first unmarshalled into the intermediary classes
            ModelUxf modelUxf = (ModelUxf) unmarshaller.unmarshal(input);
            // map the intermediary to the actual model
            Model mappedModel = modelUxfMapper.toModel(modelUxf);

            return RelationUtils.processRelations(mappedModel);
        } catch (JAXBException e) {
            throw new UxfException("Error parsing input", e.getCause());
        }
    }

    private Configuration newConfiguration(Model model) {
        Configuration configuration = new Configuration();
        String confName = UUID.randomUUID().toString();
        configuration.setName(confName);
        configuration.getModels().add(model);
        return configuration;
    }
}
