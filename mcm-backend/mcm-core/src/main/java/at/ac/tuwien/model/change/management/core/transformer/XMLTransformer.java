package at.ac.tuwien.model.change.management.core.transformer;

import jakarta.xml.bind.JAXBException;

import java.io.InputStream;

public interface XMLTransformer {

    Object unmarshal(InputStream file) throws JAXBException;

    Object unmarshal(String file) throws JAXBException;

    String marshal(Object object) throws JAXBException;

    String marshalUxf(Object object) throws JAXBException;
}
