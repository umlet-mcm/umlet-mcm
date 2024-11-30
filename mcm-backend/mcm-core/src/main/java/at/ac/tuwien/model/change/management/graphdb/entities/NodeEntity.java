package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * Node entity for the graph database. Represents one element in the UMLet Diagram.
 */
@Node("Node")
@Getter
@Setter
@NoArgsConstructor
public class NodeEntity {
    /* The id of the node, null when new node */
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    @Nullable
    private String generatedID;

    /* The name of the element in UMLet Diagram */
    private String name;

    /* The description of the element in UMLet Diagram */
    private String description;

    /* The type of the element in UMLet Diagram */
    private String type;

    @Nullable
    private String pprType;

    /* Relations with other elements in UMLet Diagram, i.e. connected with line */
    @Relationship(type = "RELATION", direction = Relationship.Direction.OUTGOING)
    private Set<RelationEntity> relations = Set.of();

    /* The properties of the element defined by user in UMLet Diagram */
    @CompositeProperty
    private Map<String,Object> properties = Map.of();

    /* The properties of the element in UMLet Diagram e.g. color */
    @CompositeProperty
    private Map<String,String> umletProperties = Map.of();

    /* The tags of the element in UMLet Diagram */
    private Set<String> tags = Set.of();

    /* The original position of the element in UMLet Diagram */
    @CompositeProperty
    private Map<String, Integer> position;
}
