package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class Node {
    private String id;
    private String text;
    private Set<Relation> relations;
    private String type;
    private Map<String, Object> properties;
    private Set<String> labels;
    private UMLetPosition umletPosition;
}
