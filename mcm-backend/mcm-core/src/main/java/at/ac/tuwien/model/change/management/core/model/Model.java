package at.ac.tuwien.model.change.management.core.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@XmlRootElement(name = "diagram")
@XmlAccessorType(XmlAccessType.FIELD)
public class Model {
    private String id; // todo
    @XmlElement(name = "element")
    private Set<Node> nodes;
}
