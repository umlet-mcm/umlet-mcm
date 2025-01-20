package at.ac.tuwien.model.change.management.core.model.versioning;

import at.ac.tuwien.model.change.management.core.model.Node;

public class NodeDiff extends BaseAttributesDiff {

    public NodeDiff(Node node, String diffType, String diff) {
        super(node, diffType, diff);
    }
}
