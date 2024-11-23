package at.ac.tuwien.model.change.management.core.model.intermediary;

import at.ac.tuwien.model.change.management.core.model.adapter.BaseAttributesAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@XmlRootElement(name = "diagram")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelUxf implements Serializable {
    @XmlElement(name = "help_text")
    @XmlJavaTypeAdapter(BaseAttributesAdapter.class)
    private BaseAttributesUxf attributes;

    @XmlElement(name = "element")
    private Set<ElementUxf> elements;
}
