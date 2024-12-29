package at.ac.tuwien.model.change.management.core.model.intermediary;

import at.ac.tuwien.model.change.management.core.model.adapter.AdditionalAttributesAdapter;
import at.ac.tuwien.model.change.management.core.model.adapter.ElementAttributesAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "element")
@XmlAccessorType(XmlAccessType.FIELD) // needed because of the Lombok getter and setter
@XmlType(propOrder = {"elementType", "umletPosition", "attributes", "generatedAttributes"})
public class ElementUxf implements Serializable {
    @XmlElement(name = "id")
    private String elementType; // e.g. UMLClass, Relation

    /**
     * Attributes defined in the panel_attributes. This includes both MCM
     * and Umlet attributes.
     */
    @XmlElement(name = "panel_attributes")
    @XmlJavaTypeAdapter(ElementAttributesAdapter.class)
    private ElementAttributesUxf attributes;

    /**
     * Attributes used internally by Umlet. E.g. the points of a relation.
     */
    @Nullable
    @XmlElement(name = "additional_attributes")
    @XmlJavaTypeAdapter(AdditionalAttributesAdapter.class)
    private List<Integer> generatedAttributes;

    @XmlElement(name = "coordinates")
    private UmletPositionUxf umletPosition;
}
