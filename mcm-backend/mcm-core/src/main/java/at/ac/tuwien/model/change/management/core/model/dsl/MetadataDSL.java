package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class MetadataDSL {

    @XmlElementWrapper(name = "panel_attributes")
    @XmlElement(name = "panel_attribute")
    private List<KeyValueDSL> panelAttributes;

    @XmlElementWrapper(name = "additional_attributes")
    @XmlElement(name = "additional_attribute", type = String.class)
    private List<Integer> additionalAttributes;

    @XmlElement(name = "coordinates", required = true)
    private CoordinatesDSL coordinates;

    @XmlElement(name = "positions")
    private PositionsDSL positions;
}