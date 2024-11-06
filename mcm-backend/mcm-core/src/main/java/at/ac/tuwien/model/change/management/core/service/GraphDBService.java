package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Node;

public interface GraphDBService {

    // TODO: probably change to ID parameter or even remove method entirely. This is mostly for testing purposes
    Node loadNode(Node node);
}
