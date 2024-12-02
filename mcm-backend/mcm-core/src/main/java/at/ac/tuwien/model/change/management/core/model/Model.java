package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Model extends BaseAttributes {

    /**
     * Needed for Umlet to correctly render the diagram.
     */
    private int zoomLevel = 10;

    private Set<Node> nodes;
}
