package at.ac.tuwien.model.change.management.core.model.intermediary;

import at.ac.tuwien.model.change.management.core.model.*;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "element")
@XmlAccessorType(XmlAccessType.FIELD) // needed because of the Lombok getter and setter
public class ElementUxf implements Serializable, McmMappable<Node> {
    @XmlElement(name = "id")
    private String elementType; // e.g. UMLClass, Relation

    /**
     * Attributes defined in the panel_attributes. This includes both MCM
     * and Umlet attributes.
     */
    @XmlElement(name = "panel_attributes")
    //@XmlJavaTypeAdapter(ElementAttributesAdapter.class)
    private ElementAttributesUxf attributes;

    /**
     * Attributes used internally by Umlet. E.g. the points of a relation.
     */
    @XmlElement(name = "additional_attributes")
    //@XmlJavaTypeAdapter(AdditionalAttributesAdapter.class)
    private List<Object> generatedAttributes;

    @XmlElement(name = "coordinates")
    private UMLetPosition umletPosition;

    /**
     * This stores the original text from the panel_attributes, in case we have to use it as a reference.
     */
    @Deprecated
    @XmlElement(name = "panel_attributes")
    private String panelAttributesFullText;

    @Override
    public Node toMcmRepresentation() {
        Node n = new Node();
        n.setElementType(this.elementType);
        n.setDescription(this.attributes.getDescription());
        n.setMcmAttributes(this.attributes.getMcmAttributes());
        n.setUmletAttributes(this.attributes.getUmletAttributes());
        n.setGeneratedAttributes(this.generatedAttributes);
        n.setUmletPosition(this.umletPosition);
        return n;
    }
}
