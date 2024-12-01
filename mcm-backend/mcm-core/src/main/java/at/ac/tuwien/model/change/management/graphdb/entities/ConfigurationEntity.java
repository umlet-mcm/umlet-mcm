package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import javax.annotation.Nullable;
import java.util.Set;

@Node("Configuration")
@Getter
@Setter
public class ConfigurationEntity {

    /* The id of the configuration, null when new configuration */
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    @Nullable
    private String id;

    /* The name of the configuration */
    private String name;

    /* The models of the configuration */
    @Relationship(type = "MODEL")
    private Set<ModelEntity> models;
}
