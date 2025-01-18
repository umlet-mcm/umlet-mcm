package at.ac.tuwien.model.change.management.core.transformer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

@Component
public class XMLTransformerImpl implements XMLTransformer {

    private final JAXBContext jaxbContext;

    public XMLTransformerImpl(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    @Override
    public Object unmarshal(InputStream file) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(file);
    }

    @Override
    public Object unmarshal(String file) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(file);
        return unmarshaller.unmarshal(reader);
    }

    @Override
    public String marshal(Object object) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();
    }

    @Override
    public String marshalUxf(Object object) throws JAXBException {
        Marshaller uxfMarshaller = jaxbContext.createMarshaller();
        uxfMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        StringWriter writer = new StringWriter();
        uxfMarshaller.marshal(object, writer);
        return writer.toString();
    }
}

