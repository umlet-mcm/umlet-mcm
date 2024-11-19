package at.ac.tuwien.model.change.management.core.model.intermediary;

import at.ac.tuwien.model.change.management.core.model.McmMappable;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@XmlRootElement(name = "diagram")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelUxf implements Serializable, McmMappable<Model> {
    @XmlElement(name = "help_text")
    //@XmlJavaTypeAdapter(ModelAttributesAdapter.class)
    private BaseAttributesUxf attributes;

    @XmlElement(name = "element")
    private Set<ElementUxf> elements;

    @Override
    public Model toMcmRepresentation() {
        Model m = new Model();
        m.setDescription(attributes.getDescription());
        m.setMcmAttributes(attributes.getMcmAttributes());
        HashSet<Node> nodes = new HashSet<>();
        for (ElementUxf e : elements) {
            nodes.add(e.toMcmRepresentation());
        }
        m.setNodes(nodes);

        return m;
    }
}
