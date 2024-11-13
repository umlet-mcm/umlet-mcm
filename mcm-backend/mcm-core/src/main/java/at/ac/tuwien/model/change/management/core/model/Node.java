package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.adapter.PanelAttributesAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter

@XmlRootElement(name = "element")
@XmlAccessorType(XmlAccessType.FIELD) // needed because of the Lombok getter and setter
public class Node {
    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "panel_attributes")
    @XmlJavaTypeAdapter(PanelAttributesAdapter.class)
    private Map<String, Object> properties;

    @XmlElement(name = "coordinates")
    private UMLetPosition umletPosition;

    // todo
    private String text;
    private Set<Relation> relations;
    private String type;
    private Set<String> labels;
}
