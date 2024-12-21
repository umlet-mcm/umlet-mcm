package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * The entity class for the model.
 */
@Node("Model")
@Getter
@Setter
public class ModelEntity {
    /* The id of the model, null when new model */
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    @Nullable
    private String id;

    /* The nodes of the model */
    @Relationship(type = "NODE")
    private Set<NodeEntity> nodes;

    /* The tags of the whole diagram in UMLet Diagram */
    private Set<String> tags = Set.of();

    /* The properties of the whole model in UMLet Diagram */
    @CompositeProperty
    private Map<String, Value> properties = Map.of();

    /* The name of the model */
    private String name;

    /* The description of the model */
    private String description;
}
