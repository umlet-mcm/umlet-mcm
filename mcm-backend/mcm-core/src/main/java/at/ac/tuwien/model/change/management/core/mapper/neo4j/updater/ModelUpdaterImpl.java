package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Model;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelUpdaterImpl implements ModelUpdater {
    private final NodeUpdater nodeUpdater;

    @Override
    public void updateModel(Model model, Model modelToUpdate) {
        if(model == null || modelToUpdate == null) {
            return;
        }

        // Assign ID
        if(model.getId() != null) {
            modelToUpdate.setId( modelToUpdate.getId() );
        }

        // Assign nodes
        for (val nodeToUpdate : modelToUpdate.getNodes()) {
            // Find the corresponding model entity
            val node = model.getNodes().stream()
                    .filter(nodeItem -> {
                        assert nodeItem.getId() != null;
                        return nodeItem.getId().equals(nodeToUpdate.getId());
                    })
                    .findFirst()
                    .orElse(null);
            nodeUpdater.updateNode(node, nodeToUpdate);
        }

        // Assign tags
        modelToUpdate.setTags( model.getTags());

        // Assign properties
        modelToUpdate.setMcmAttributes( model.getMcmAttributes() );

        // Assign name
        modelToUpdate.setTitle(model.getTitle());

        // Assign description
        modelToUpdate.setDescription(model.getDescription());
    }
}
