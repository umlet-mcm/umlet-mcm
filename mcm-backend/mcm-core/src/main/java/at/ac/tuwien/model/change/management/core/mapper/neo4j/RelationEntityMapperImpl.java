package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Relation;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
import lombok.val;
import org.neo4j.driver.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
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
            relationEntity.setId( relation.getId() );
        }
        relationEntity.setName( relation.getTitle() );
        relationEntity.setDescription( relation.getDescription() );
        relationEntity.setType( relation.getType() );
        if(relation.getMcmAttributes() != null) {
            val mcmAttributes = new HashMap<String, Value>() {{
                relation.getMcmAttributes().forEach((key, value) -> put(key, Neo4jValueConverter.convertObject(value)));
            }};
            relationEntity.setProperties( mcmAttributes );
        }
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

        if(relationEntity.getId() != null) {
            relation.setId( relationEntity.getId() );
        }
        relation.setTitle( relationEntity.getName() );
        relation.setDescription( relationEntity.getDescription() );
        relation.setType( relationEntity.getType() );
        val mcmAttributes  = new LinkedHashMap<String,Object>() {{
            relationEntity.getProperties().forEach((key, value) -> put(key, Neo4jValueConverter.convertValue((Value) value)));
        }};
        relation.setMcmAttributes( mcmAttributes );
        relation.setUmletAttributes( new LinkedHashMap<>(relationEntity.getUmletProperties()) );
        relation.setTags( new ArrayList<>(relationEntity.getTags()) );
        relation.setTarget( nodeMapper.fromEntity( relationEntity.getTarget() ));

        return relation;
    }
}
