package at.ac.tuwien.model.change.management.graphdb.entities;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.core.model.UMLetPosition;
import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Map;
import java.util.Set;

/**
 * Node entity for the graph database. Represents one element in the UMLet Diagram.
 */
@Node("Node")
@Getter
@Setter
public class NodeEntity {
    @Id @GeneratedValue
    private Long generatedID;

    /* The name of the element in UMLet Diagram */
    private String name;

    /* The description of the element in UMLet Diagram */
    private String description;

    /* The type of the element in UMLet Diagram */
    private String type;

    /* Relations with other elements in UMLet Diagram, i.e. connected with line */
    @Relationship(type = "RELATION")
    private Set<NodeEntity> relations;

    /* The properties of the element in UMLet Diagram */
    private Set<String> properties;

    /* The original position of the element in UMLet Diagram */
    @CompositeProperty
    private Map<String, Integer> position;
}
