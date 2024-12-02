package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.mapper.neo4j.NodeEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.dao.NodeEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.RawNeo4jService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GraphDBServiceImpl implements GraphDBService {

    private final NodeEntityDAO nodeEntityDAO;
    private final NodeEntityMapper nodeEntityMapper;
    private final RawNeo4jService rawNeo4jService;

    @Override
    public Node loadNode(@NonNull Node node) {
        var nodeEntity = nodeEntityDAO.save(nodeEntityMapper.toEntity(node));
        return nodeEntityMapper.fromEntity(nodeEntity);
    }

    @Override
    public List<Map<String,Object>> executeQuery(String query) {
        val response = rawNeo4jService.executeRawQuery(query);
        return response;
    }
}
