package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;

/**
 * Mapper for converting between Relation and RelationEntity
 */
public interface RelationEntityMapper {
    /**
     * Converts a Relation to a RelationEntity
     *
     * @param relation the Relation to convert
     * @return the converted RelationEntity
     */
    RelationEntity toEntity(Relation relation);

    /**
     * Converts a RelationEntity to a Relation
     *
     * @param relationEntity the RelationEntity to convert
     * @return the converted Relation
     */
    Relation fromEntity(RelationEntity relationEntity);
}
