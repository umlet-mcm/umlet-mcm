package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@XmlRootElement(name = "node")
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeDSL {
    private String id;
    private String text;

    @XmlElement(name = "element_type")
    private String elementType;

    @XmlElement(name = "mcm_type")
    private String mcmType;

    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    private Set<String> tags;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<PropertyDSL> properties;

    @NotNull
    @XmlElement(name = "metadata", required = true)
    private MetadataDSL metadata;
}