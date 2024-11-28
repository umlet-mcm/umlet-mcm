package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "model")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelDSL {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "text")
    private String text;

    @XmlElement(name = "mcm_type")
    private String mcmType;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<PropertyDSL> properties;
}
