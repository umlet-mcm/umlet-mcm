package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationGetException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.mapper.uxf.ModelUxfMapper;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
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

import java.io.File;
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
            Model modelWithRelations = RelationUtils.processRelations(mappedModel);


            Configuration newConf = null;
            if(modelWithRelations.getId()==null){
                newConf = newConfiguration(modelWithRelations);
            } else{
                try{
                    Configuration existingConf = configurationService.getConfigurationByName(modelWithRelations.getId());
                    existingConf.getModels().add(modelWithRelations);
                    configurationService.updateConfiguration(existingConf);

                } catch (ConfigurationNotFoundException e){
                    newConf = newConfiguration(modelWithRelations);
                } catch (ConfigurationGetException e){

                }
            }

            if(newConf!=null){
                configurationService.createConfiguration(newConf);
            }






            // map the model back to the intermediary representation
            ModelUxf resUxf = modelUxfMapper.fromModel(modelWithRelations);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // do not generate <xml> header
            File out = new File( "export.xml" );

            // marshal it to a file
            marshaller.marshal(resUxf, out);

            //ParserUtils.getConfigurationUxf(null);

            log.debug(resUxf.toString());
            log.debug(modelUxf.toString());
        } catch (JAXBException e) {
            throw new UxfException("Error parsing input", e.getCause());
        }

        return null;
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
