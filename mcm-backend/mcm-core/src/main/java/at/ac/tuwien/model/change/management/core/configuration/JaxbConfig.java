package at.ac.tuwien.model.change.management.core.configuration;

import at.ac.tuwien.model.change.management.core.model.dsl.ModelDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.NodeDSL;
import at.ac.tuwien.model.change.management.core.model.dsl.RelationDSL;
import at.ac.tuwien.model.change.management.core.model.intermediary.ModelUxf;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaxbConfig {

    @Bean
    public JAXBContext jaxbContext() throws JAXBException {
        return JAXBContext.newInstance(
                ModelDSL.class,
                NodeDSL.class,
                RelationDSL.class,
                ModelUxf.class
        );
    }
}
