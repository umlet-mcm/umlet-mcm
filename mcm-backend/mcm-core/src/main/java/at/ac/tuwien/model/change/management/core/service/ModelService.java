package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;

import java.util.List;

public interface ModelService {

    /**
     * Delete a model
     *
     * @param modelId the id of the model to be deleted
     */
    Configuration deleteModel(String modelId);

    /**
     * Align a list of models
     *
     * @param models the list of models to be aligned
     * @return the list of aligned models
     */
    List<Model> alignModels(List<Model> models);
}
