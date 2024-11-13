package at.ac.tuwien.model.change.management.core.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

// JAXB expects a no args constructor so this record was changed to a class

@Getter
@Setter

@XmlRootElement(name = "coordinates")
@XmlAccessorType(XmlAccessType.FIELD)
public class UMLetPosition {
    @XmlElement
    int x;
    @XmlElement
    int y;
    @XmlElement(name = "w")
    int width;
    @XmlElement(name = "h")
    int height;
}
