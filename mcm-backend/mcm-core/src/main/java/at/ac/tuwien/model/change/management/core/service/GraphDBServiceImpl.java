package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.mapper.neo4j.NodeEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.dao.NodeEntityDAO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraphDBServiceImpl implements GraphDBService {

    private final NodeEntityDAO nodeEntityDAO;
    private final NodeEntityMapper nodeEntityMapper;

    @Override
    public Node loadNode(@NonNull Node node) {
        var nodeEntity = nodeEntityDAO.save(nodeEntityMapper.toEntity(node));
        return nodeEntityMapper.fromEntity(nodeEntity);
    }
}
