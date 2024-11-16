package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("Model")
@Getter
@Setter
public class ModelEntity {
    @Id @GeneratedValue
    private String id;

    /* The nodes of the model */
    @Relationship(type = "NODE")
    private Set<NodeEntity> nodes;
}
