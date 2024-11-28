package at.ac.tuwien.model.change.management.core.model.dsl;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "relation")
@XmlAccessorType(XmlAccessType.FIELD)
public class RelationDSL {
    private String id;
    private String text;

    @XmlElement(name = "element_type")
    private String elementType;

    @XmlElement(name = "mcm_type")
    private String mcmType;

    @XmlElement(name = "source")
    private RelationEndpointDSL source;

    @XmlElement(name = "target")
    private RelationEndpointDSL target;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<PropertyDSL> properties;

    @NotNull
    @XmlElement(name = "metadata", required = true)
    private MetadataDSL metadata;
}
