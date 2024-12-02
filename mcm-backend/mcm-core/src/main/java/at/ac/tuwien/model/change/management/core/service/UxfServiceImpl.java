package at.ac.tuwien.model.change.management.core.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class UxfServiceImpl implements UxfService {
    private ModelUxfMapper modelUxfMapper;

    @Autowired
    public UxfServiceImpl(ModelUxfMapper modelUxfMapper) {
        this.modelUxfMapper = modelUxfMapper;
    }

    @Override
    public Configuration createConfigurationFromUxf(InputStreamSource file) {
        InputStream input;
        JAXBContext context;
        Unmarshaller unmarshaller;
        try {
            context = JAXBContext.newInstance(ModelUxf.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            return null;
        }

        try {
            input = file.getInputStream();
        } catch (IOException e) {
            log.error("Error opening file '" + file + "'");
            return null;
        }

        try {
            // the uxf is first unmarshalled into the intermediary classes
            ModelUxf modelUxf = (ModelUxf) unmarshaller.unmarshal(input);
            // map the intermediary to the actual model
            Model model = modelUxfMapper.toModel(modelUxf);
            Model wr = RelationUtils.processRelations(model);

            // map the model back to the intermediary representation
            ModelUxf resUxf = modelUxfMapper.fromModel(wr);

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // do not generate <xml> header
            File out = new File( "export.xml" );

            // marshal it to a file
            marshaller.marshal(resUxf, out);

            log.debug(resUxf.toString());
            log.debug(modelUxf.toString());
        } catch (JAXBException e) {
            return null;
        }

        return null;
    }


}
