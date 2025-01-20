package at.ac.tuwien.model.change.management.core.model.versioning;

import at.ac.tuwien.model.change.management.core.model.Relation;

public class RelationDiff extends BaseAttributesDiff {

    public RelationDiff(Relation relation, String diffType, String diff) {
        super(relation, diffType, diff);
    }
}
