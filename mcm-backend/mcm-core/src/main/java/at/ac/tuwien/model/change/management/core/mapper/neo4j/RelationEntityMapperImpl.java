package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

@Component
public class RelationEntityMapperImpl implements RelationEntityMapper {

    @Autowired
    private NodeEntityMapper nodeMapper;


    @Override
    public RelationEntity toEntity(Relation relation) {
        if(relation == null) {
            return null;
        }

        RelationEntity relationEntity = new RelationEntity();

        if(relation.getId() != null) {
            //relationEntity.setGraphId( relation.getId() );
        }
        relationEntity.setName( relation.getTitle() );
        relationEntity.setDescription( relation.getDescription() );
        relationEntity.setType( relation.getType() );
        if(relation.getMcmAttributes() != null)
            relationEntity.setProperties( relation.getMcmAttributes() );
        if(relation.getUmletAttributes() != null)
            relationEntity.setUmletProperties( relation.getUmletAttributes() );
        if(relation.getTags() != null)
            relationEntity.setTags( new HashSet<>(relation.getTags()));
        relationEntity.setTarget( nodeMapper.toEntity( relation.getTarget() ) );

        return relationEntity;
    }

    @Override
    public Relation fromEntity(RelationEntity relationEntity) {
        if(relationEntity == null) {
            return null;
        }

        Relation relation = new Relation();

        if(relationEntity.getGraphId() != null) {
            relation.setId( relationEntity.getGraphId() );
        }
        relation.setTitle( relationEntity.getName() );
        relation.setDescription( relationEntity.getDescription() );
        relation.setType( relationEntity.getType() );
        relation.setMcmAttributes( new LinkedHashMap<>(relationEntity.getProperties()) );
        relation.setUmletAttributes( new LinkedHashMap<>(relationEntity.getUmletProperties()) );
        relation.setTags( new ArrayList<>(relationEntity.getTags()) );
        relation.setTarget( nodeMapper.fromEntity( relationEntity.getTarget() ));

        return relation;
    }
}
