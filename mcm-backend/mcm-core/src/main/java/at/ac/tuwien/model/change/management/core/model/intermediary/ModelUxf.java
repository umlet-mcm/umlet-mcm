package at.ac.tuwien.model.change.management.core.model.intermediary;

import at.ac.tuwien.model.change.management.core.model.adapter.BaseAttributesAdapter;
import jakarta.xml.bind.annotation.*;
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


    @XmlAttribute(name = "version")
    public float _version;
    @XmlAttribute(name = "program")
    public String _program;

    @XmlElement(name = "help_text")
    @XmlJavaTypeAdapter(BaseAttributesAdapter.class)
    private BaseAttributesUxf attributes;

    @XmlElement(name = "element")
    private Set<ElementUxf> elements;
}
