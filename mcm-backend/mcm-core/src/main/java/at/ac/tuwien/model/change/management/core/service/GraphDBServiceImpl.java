package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.mapper.neo4j.NodeEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Node;
import at.ac.tuwien.model.change.management.graphdb.dao.NodeEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.dao.RawNeo4jService;
import com.google.gson.Gson;
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
    public List<Map<String, String>> executeQuery(String query) {
        val response = rawNeo4jService.executeRawQuery(query);
        val convert = new ArrayList<Map<String,String>>();
        Gson gson = new Gson();
        for (var record : response) {
            val map = new HashMap<String, String>();
            for (var key : record.keySet()) {
                map.put(key, gson.toJson(record.get(key)));
            }
            convert.add(map);
        }
        return convert;
    }
}
