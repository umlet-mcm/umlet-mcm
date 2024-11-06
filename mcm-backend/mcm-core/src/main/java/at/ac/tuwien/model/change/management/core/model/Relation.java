package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Relation {
    private String type;
    private Node source;
    private Node target;
    private UMLetPosition umletPosition;
}
