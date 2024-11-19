package at.ac.tuwien.model.change.management.core.mapper;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class RelationEntityMapperImpl implements RelationEntityMapper {

    @Autowired
    private NodeEntityMapper nodeMapper;


    @Override
    public RelationEntity toEntity(Relation relation) {
        RelationEntity relationEntity = new RelationEntity();

        relationEntity.setType( relation.getType() );
        relationEntity.setTarget( nodeMapper.toEntity( relation.getTarget() ) );

        return relationEntity;
    }

    @Override
    public Relation fromEntity(RelationEntity relationEntity) {
        Relation relation = new Relation();

        relation.setType( relationEntity.getType() );

        relation.setTarget( nodeMapper.fromEntity( relationEntity.getTarget() ));

        return relation;
    }
}
