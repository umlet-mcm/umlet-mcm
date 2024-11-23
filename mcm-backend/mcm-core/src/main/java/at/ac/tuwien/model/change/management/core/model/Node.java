package at.ac.tuwien.model.change.management.core.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java representation for 'element' types in the uxf files. Initially every element is parsed
 * into nodes, event those that have the Umlet type 'Relation'. These are later extracted and
 * turned into real relations.
 */
@Getter
@Setter
public class Node extends BaseAttributes {
    private String elementType;
    private Map<String, String> umletAttributes;

    /**
     * Attributes used internally by Umlet. E.g. the points of a relation.
     */
    private List<Object> generatedAttributes;
    private UmletPosition umletPosition;
    private Set<Relation> relations = new HashSet<>();
}
