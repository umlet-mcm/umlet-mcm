package at.ac.tuwien.model.change.management.core.model.versioning;


import at.ac.tuwien.model.change.management.core.model.Model;


public class ModelDiff extends BaseAttributesDiff {

    public ModelDiff(Model model, String diffType, String diff) {
        super(model, diffType, diff);
    }
}
