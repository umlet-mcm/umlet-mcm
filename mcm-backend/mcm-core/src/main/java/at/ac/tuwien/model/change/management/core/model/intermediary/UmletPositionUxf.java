package at.ac.tuwien.model.change.management.core.model.intermediary;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@XmlRootElement(name = "coordinates")
@XmlAccessorType(XmlAccessType.FIELD)
public class UmletPositionUxf {
    @XmlElement
    int x;
    @XmlElement
    int y;
    @XmlElement(name = "w")
    int width;
    @XmlElement(name = "h")
    int height;
}
