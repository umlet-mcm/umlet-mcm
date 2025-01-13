package at.ac.tuwien.model.change.management.core.mapper.neo4j.updater;

import at.ac.tuwien.model.change.management.core.model.Model;

public interface ModelUpdater {
    /**
     * Updates a Model with the values of a Model from DB
     *
     * @param model         the Model to update from
     * @param modelToUpdate the Model to update
     */
    void updateModel(Model model, Model modelToUpdate);
}
