package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import javax.annotation.Nullable;

/**
 * Entity representing a relation in the database.
 */
@RelationshipProperties
@Getter
@Setter
public class RelationEntity {
    /* The id of the relation, null when new relation */
    @Id
    @GeneratedValue
    @Nullable
    private Long graphId;

    /* The type of the relation */
    private String type;

    /* The target of the relation */
    @TargetNode
    private NodeEntity target;
}
