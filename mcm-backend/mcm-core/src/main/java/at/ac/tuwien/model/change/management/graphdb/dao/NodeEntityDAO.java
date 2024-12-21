package at.ac.tuwien.model.change.management.graphdb.dao;

import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NodeEntityDAO extends Neo4jRepository<NodeEntity, String> {
    @Query("MATCH (n:Node)-[:RELATION]->(m:Node) RETURN n")
    List<NodeEntity> getNodesWithOutgoingRelations();

    @Query("MATCH (n:Node)<-[:RELATION]-(m:Node) RETURN n")
    List<NodeEntity> getNodesWithIncomingRelations();

    @Query("              MATCH (leaf:Node {generatedID: $nodeID})\n" +
            "             OPTIONAL MATCH path = (leaf)<-[:RELATION*]-(predecessor)\n" +
            "             WITH leaf, collect(predecessor) AS predecessors\n" +
            "             UNWIND predecessors AS node\n" +
            "             RETURN collect(node)")
    List<NodeEntity> getPredecessors(
            @Param("nodeID") String nodeID);
}
