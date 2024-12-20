package at.ac.tuwien.model.change.management.core.model;

import at.ac.tuwien.model.change.management.core.model.attributes.ElementAttributes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Java representation for 'element' types in the uxf files. Initially every element is parsed
 * into nodes, event those that have the Umlet type 'Relation'. These are later extracted and
 * turned into real relations.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Node extends ElementAttributes {
    private String elementType;

    /**
     * Attributes used internally by Umlet. E.g. the points of a relation.
     */
    private List<Integer> generatedAttributes;

    private UMLetPosition umletPosition;

    private Set<Relation> relations = new HashSet<>();
}
