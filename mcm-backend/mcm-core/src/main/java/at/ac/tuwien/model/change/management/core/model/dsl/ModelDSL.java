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

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "description")
    private String description;

    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    private List<String> tags;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<PropertyDSL> properties;

    @XmlElement(name = "metadata")
    private MetadataDSL metadata;

    @XmlElement(name = "zoom_level")
    private int zoomLevel;
}
