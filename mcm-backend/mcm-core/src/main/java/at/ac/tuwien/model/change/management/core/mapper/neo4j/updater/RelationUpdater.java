package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Relation;

public interface RelationUpdater {
    /**
     * Updates a Relation with the values of a Relation from DB
     *
     * @param relation         the Relation to update from
     * @param relationToUpdate the Relation to update
     */
    void updateRelation(Relation relation, Relation relationToUpdate);
}
