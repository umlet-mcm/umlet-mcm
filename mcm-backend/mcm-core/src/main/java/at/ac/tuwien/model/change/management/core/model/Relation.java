package at.ac.tuwien.model.change.management.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Relation {
    private String type;
    private Node source;
    private Node target;
    private UMLetPosition umletPosition;
}
