package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "node")
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeDSL {
    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "mcm_model")
    private String mcmModel;

    @XmlElement(name = "mcm_model_id")
    private String mcmModelId;

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "element_type")
    private String elementType;

    @XmlElement(name = "ppr_type")
    private String pprType;

    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    private List<String> tags;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<PropertyDSL> properties;

    @XmlElement(name = "metadata")
    private MetadataDSL metadata;
}