package at.ac.tuwien.model.change.management.core.model.intermediary;

import at.ac.tuwien.model.change.management.core.model.adapter.BaseAttributesAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

/**
 * Uxf representation of a configuration which is created by
 * merging multiple models.
 */
@Getter
@Setter
@XmlRootElement(name = "diagram")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"_zoomLevel", "attributes", "elements"})
public class ConfigurationUxf implements Serializable {

    @XmlElement(name = "zoom_level")
    private int _zoomLevel;

    @XmlElement(name = "help_text")
    @XmlJavaTypeAdapter(BaseAttributesAdapter.class)
    private BaseAttributesUxf attributes;

    @XmlElement(name = "element")
    private Set<ElementUxf> elements;

}
