package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Relation;
import org.springframework.stereotype.Component;

@Component
public class RelationUpdaterImpl implements RelationUpdater {
    @Override
    public void updateRelation(Relation relation, Relation relationToUpdate) {
        if(relation == null || relationToUpdate == null) {
            return;
        }

        relationToUpdate.setId( relation.getId() );
        relationToUpdate.setTitle( relation.getTitle() );
        relationToUpdate.setDescription( relation.getDescription() );
        relationToUpdate.setType( relation.getType() );
        relationToUpdate.setMcmAttributes( relation.getMcmAttributes() );
        relationToUpdate.setUmletAttributes( relation.getUmletAttributes() );
        relationToUpdate.setTags( relation.getTags() );
        relationToUpdate.setTarget( relation.getTarget() );
    }
}
