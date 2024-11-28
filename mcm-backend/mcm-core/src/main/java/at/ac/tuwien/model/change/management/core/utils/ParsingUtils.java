package at.ac.tuwien.model.change.management.core.utils;

import at.ac.tuwien.model.change.management.core.model.dsl.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

public class ParsingUtils {

    public static Object unmarshalDSL(String DSLFile) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(
                ModelDSL.class,
                NodeDSL.class,
                RelationDSL.class,
                CoordinatesDSL.class,
                MetadataDSL.class,
                PanelAttributeDSL.class,
                PropertyDSL.class,
                RelationEndpointDSL.class
        );
        Unmarshaller unmarshaller = context.createUnmarshaller();
        unmarshaller.setEventHandler(event -> {
            throw new RuntimeException("ERROR: " + event.getMessage());
        });

        StringReader reader = new StringReader(DSLFile);

        return unmarshaller.unmarshal(reader);
    }

    public static String marshalDSL(Object dslObject) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(
                ModelDSL.class,
                NodeDSL.class,
                RelationDSL.class,
                CoordinatesDSL.class,
                MetadataDSL.class,
                PanelAttributeDSL.class,
                PropertyDSL.class,
                RelationEndpointDSL.class
        );

        var marshaller = context.createMarshaller();

        marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

        Writer writer = new StringWriter();
        marshaller.marshal(dslObject, writer);

        return writer.toString();
    }
}
