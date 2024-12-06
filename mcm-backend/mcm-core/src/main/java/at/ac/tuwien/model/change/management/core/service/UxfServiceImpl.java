package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.ConfigurationGetException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.mapper.uxf.ModelUxfMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import at.ac.tuwien.model.change.management.core.model.utils.RelationUtils;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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
        try{
            target = configurationService.getConfigurationByName(configurationUUID);
        } catch (ConfigurationGetException e){
            throw new ConfigurationException("Could not get configuration",e.getCause());
        }

        Model parsedModel = parseUxf(file);
        target.getModels().add(parsedModel);
        configurationService.updateConfiguration(target);
        return target;
    }

    private Model parseUxf(InputStreamSource file) throws UxfException{
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

    private Configuration newConfiguration(Model model){
        Configuration configuration = new Configuration();
        String confName = UUID.randomUUID().toString();
        configuration.setName(confName);
        //model.setId(confName);
        configuration.getModels().add(model);
        return configuration;
    }
}
