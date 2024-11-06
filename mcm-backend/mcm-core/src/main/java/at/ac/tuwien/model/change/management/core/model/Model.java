package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Model {
    private String id;
    private Set<Node> nodes;
}
