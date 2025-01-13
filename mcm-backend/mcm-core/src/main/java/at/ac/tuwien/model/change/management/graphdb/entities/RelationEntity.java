package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Map;
import java.util.Set;

/**
 * Entity representing a relation in the database.
 */
@RelationshipProperties
@Getter
@Setter
public class RelationEntity {
    /* The id of the relation, null when new relation */
    @RelationshipId
    private String graphId;

    /* The id of the relation within GIT repository, null when new relation */
    private String id;

    /* The name of the relation in UMLet Diagram */
    private String name;

    /* The description of the relation in UMLet Diagram */
    private String description;

    /* The type of the relation */
    private String type;

    /* The properties of the relation in UMLet Diagram */
    @CompositeProperty
    private Map<String, Value> properties = Map.of();

    /* The attributes of the relation in UMLet Diagram e.g. color */
    @CompositeProperty
    private Map<String,String> umletProperties = Map.of();

    /* The tags of the relation in UMLet Diagram */
    private Set<String> tags = Set.of();

    /* The target of the relation */
    @TargetNode
    private NodeEntity target;
}
