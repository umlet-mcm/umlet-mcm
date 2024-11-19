package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@XmlRootElement(name = "diagram")
@XmlAccessorType(XmlAccessType.FIELD)
public class Model {
    @XmlElement(name = "help_text")
    //@XmlJavaTypeAdapter(ModelAttributesAdapter.class)
    private BaseAttributes attributes;

    @XmlElement(name = "element")
    private Set<Node> nodes;
}
