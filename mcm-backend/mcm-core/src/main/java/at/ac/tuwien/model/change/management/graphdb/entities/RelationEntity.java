package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

/**
 * Entity representing a relation in the database.
 */
@RelationshipProperties
@Getter
@Setter
public class RelationEntity {
    @Id
    @GeneratedValue
    private Long graphId;
    private String type;

    @TargetNode
    private NodeEntity target;
}
