package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.ElementAttributes;
import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Java representation for 'element' types in the uxf files. Initially every element is parsed
 * into nodes, event those that have the Umlet type 'Relation'. These are later extracted and
 * turned into real relations.
 */
@Getter
@Setter

@XmlRootElement(name = "element")
@XmlAccessorType(XmlAccessType.FIELD) // needed because of the Lombok getter and setter
public class Node {
    @XmlElement(name = "id")
    private String elementType; // e.g. UMLClass, Relation

    /**
     * Attributes defined in the panel_attributes. This includes both MCM
     * and Umlet attributes.
     */
    @XmlElement(name = "panel_attributes")
    //@XmlJavaTypeAdapter(NodeAttributesAdapter.class)
    private ElementAttributes attributes;

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
     *
     * @deprecated {@link #attributes} should be used for accessing node attributes
     */
    @Deprecated
    @XmlElement(name = "panel_attributes")
    private String panelAttributesFullText;

    private Set<Relation> relations = new HashSet<>();
}
