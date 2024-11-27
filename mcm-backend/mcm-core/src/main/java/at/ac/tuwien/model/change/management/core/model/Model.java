package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.BaseAttributes;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Model extends BaseAttributes {
    private Set<Node> nodes;
}
